package reservation_service.kafka;

import reservation_service.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consomme les événements de suppression émis par Room Service et Member Service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationKafkaConsumer {

    private final ReservationService reservationService;

    /**
     * Quand une salle est supprimée, toutes ses réservations CONFIRMED
     * sont automatiquement annulées (CANCELLED).
     */
    @KafkaListener(topics = "room-deleted", groupId = "reservation-service-group")
    public void handleRoomDeleted(ReservationEvents.RoomDeletedEvent event) {
        log.info("Réception room-deleted pour la salle {}", event.getRoomId());
        reservationService.cancelAllConfirmedForRoom(event.getRoomId());
    }

    /**
     * Quand un membre est supprimé, toutes ses réservations sont supprimées.
     */
    @KafkaListener(topics = "member-deleted", groupId = "reservation-service-group")
    public void handleMemberDeleted(ReservationEvents.MemberDeletedEvent event) {
        log.info("Réception member-deleted pour le membre {}", event.getMemberId());
        reservationService.deleteAllForMember(event.getMemberId());
    }
}
