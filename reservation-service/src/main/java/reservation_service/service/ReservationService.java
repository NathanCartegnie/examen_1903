package reservation_service.service;

import reservation_service.dto.ReservationRequest;
import reservation_service.entity.Reservation;
import reservation_service.entity.ReservationStatus;
import reservation_service.kafka.ReservationKafkaProducer;
import reservation_service.repository.ReservationRepository;
import reservation_service.statepattern.ReservationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationKafkaProducer kafkaProducer;
    private final RestTemplate restTemplate;

    @Value("${services.room-service.url:http://room-service}")
    private String roomServiceUrl;

    @Value("${services.member-service.url:http://member-service}")
    private String memberServiceUrl;

    // ─────────────────────────────────────────────
    // Création d'une réservation
    // ─────────────────────────────────────────────

    public Reservation createReservation(ReservationRequest request) {
        validateDates(request);

        // 1. Vérifier que la salle est disponible (appel REST vers Room Service)
        Boolean roomAvailable = restTemplate.getForObject(
                roomServiceUrl + "/api/rooms/" + request.getRoomId() + "/available",
                Boolean.class
        );
        if (Boolean.FALSE.equals(roomAvailable)) {
            throw new RuntimeException("La salle " + request.getRoomId() + " n'est pas disponible");
        }

        // 2. Vérifier qu'il n'y a pas de conflit de créneau
        boolean hasConflict = reservationRepository.existsConflictingReservation(
                request.getRoomId(), request.getStartDateTime(), request.getEndDateTime()
        );
        if (hasConflict) {
            throw new RuntimeException(
                    "La salle " + request.getRoomId() + " est déjà réservée sur ce créneau");
        }

        // 3. Vérifier que le membre n'est pas suspendu (appel REST vers Member Service)
        Boolean memberSuspended = restTemplate.getForObject(
                memberServiceUrl + "/api/members/" + request.getMemberId() + "/suspended",
                Boolean.class
        );
        if (Boolean.TRUE.equals(memberSuspended)) {
            throw new RuntimeException(
                    "Le membre " + request.getMemberId() + " est suspendu et ne peut plus réserver");
        }

        // 4. Créer et persister la réservation
        Reservation reservation = new Reservation();
        reservation.setRoomId(request.getRoomId());
        reservation.setMemberId(request.getMemberId());
        reservation.setStartDateTime(request.getStartDateTime());
        reservation.setEndDateTime(request.getEndDateTime());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = reservationRepository.save(reservation);

        // 5. Rendre la salle indisponible via Kafka
        kafkaProducer.sendRoomAvailabilityEvent(request.getRoomId(), false);

        // 6. Vérifier le quota du membre et le suspendre si nécessaire
        long activeBookings = reservationRepository.countByMemberIdAndStatus(
                request.getMemberId(), ReservationStatus.CONFIRMED
        );
        // Récupérer le maxConcurrentBookings du membre
        int maxBookings = getMaxBookingsForMember(request.getMemberId());
        if (activeBookings >= maxBookings) {
            log.info("Membre {} a atteint son quota ({}/{}), suspension en cours",
                    request.getMemberId(), activeBookings, maxBookings);
            kafkaProducer.sendMemberSuspensionEvent(request.getMemberId(), true);
        }

        return saved;
    }

    // ─────────────────────────────────────────────
    // Consultation
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable avec l'id : " + id));
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByMember(Long memberId) {
        return reservationRepository.findByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByRoom(Long roomId) {
        return reservationRepository.findByRoomId(roomId);
    }

    // ─────────────────────────────────────────────
    // Annulation — via State Pattern
    // ─────────────────────────────────────────────

    public Reservation cancelReservation(Long id) {
        Reservation reservation = getReservationById(id);

        // Utilisation du State Pattern : délègue la logique de transition à l'état courant
        ReservationContext context = new ReservationContext(reservation);
        context.cancel(); // Lève IllegalStateException si déjà annulée ou complétée

        Reservation saved = reservationRepository.save(reservation);

        // Libérer la salle via Kafka
        kafkaProducer.sendRoomAvailabilityEvent(reservation.getRoomId(), true);

        // Désuspendre le membre si son quota repasse en dessous du maximum
        checkAndUnsuspendMember(reservation.getMemberId());

        return saved;
    }

    // ─────────────────────────────────────────────
    // Complétion — via State Pattern
    // ─────────────────────────────────────────────

    public Reservation completeReservation(Long id) {
        Reservation reservation = getReservationById(id);

        // Utilisation du State Pattern : délègue la logique de transition à l'état courant
        ReservationContext context = new ReservationContext(reservation);
        context.complete(); // Lève IllegalStateException si déjà annulée ou complétée

        Reservation saved = reservationRepository.save(reservation);

        // Libérer la salle via Kafka
        kafkaProducer.sendRoomAvailabilityEvent(reservation.getRoomId(), true);

        // Désuspendre le membre si son quota repasse en dessous du maximum
        checkAndUnsuspendMember(reservation.getMemberId());

        return saved;
    }

    // ─────────────────────────────────────────────
    // Propagation Kafka entrante
    // ─────────────────────────────────────────────

    /**
     * Appelé par le consumer quand un événement room-deleted est reçu.
     * Annule toutes les réservations CONFIRMED de cette salle.
     */
    public void cancelAllConfirmedForRoom(Long roomId) {
        List<Reservation> confirmed = reservationRepository
                .findByRoomIdAndStatus(roomId, ReservationStatus.CONFIRMED);

        for (Reservation reservation : confirmed) {
            ReservationContext context = new ReservationContext(reservation);
            context.cancel();
            reservationRepository.save(reservation);

            // Désuspendre les membres concernés
            checkAndUnsuspendMember(reservation.getMemberId());
        }
        log.info("{} réservation(s) annulée(s) suite à la suppression de la salle {}", confirmed.size(), roomId);
    }

    /**
     * Appelé par le consumer quand un événement member-deleted est reçu.
     * Supprime toutes les réservations du membre.
     */
    public void deleteAllForMember(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        // Libérer les salles des réservations CONFIRMED avant suppression
        reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .forEach(r -> kafkaProducer.sendRoomAvailabilityEvent(r.getRoomId(), true));
        reservationRepository.deleteAll(reservations);
        log.info("{} réservation(s) supprimée(s) suite à la suppression du membre {}", reservations.size(), memberId);
    }

    // ─────────────────────────────────────────────
    // Méthodes privées utilitaires
    // ─────────────────────────────────────────────

    private void validateDates(ReservationRequest request) {
        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {
            throw new RuntimeException("La date de fin doit être postérieure à la date de début");
        }
    }

    /**
     * Récupère le maxConcurrentBookings du membre via le Member Service.
     * En cas d'échec (service indisponible), on utilise la valeur par défaut BASIC = 2.
     */
    private int getMaxBookingsForMember(Long memberId) {
        try {
            var member = restTemplate.getForObject(
                    memberServiceUrl + "/api/members/" + memberId,
                    java.util.Map.class
            );
            if (member != null && member.containsKey("maxConcurrentBookings")) {
                return (Integer) member.get("maxConcurrentBookings");
            }
        } catch (Exception e) {
            log.warn("Impossible de récupérer le quota du membre {}, utilisation de la valeur par défaut", memberId);
        }
        return 2; // BASIC par défaut
    }

    /**
     * Vérifie si le membre peut être désuspendu (quota repassé sous le max).
     */
    private void checkAndUnsuspendMember(Long memberId) {
        long activeBookings = reservationRepository.countByMemberIdAndStatus(
                memberId, ReservationStatus.CONFIRMED
        );
        int maxBookings = getMaxBookingsForMember(memberId);
        if (activeBookings < maxBookings) {
            log.info("Membre {} désuspendu ({}/{} réservations actives)", memberId, activeBookings, maxBookings);
            kafkaProducer.sendMemberSuspensionEvent(memberId, false);
        }
    }
}
