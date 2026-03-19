package reservation_service.repository;

import reservation_service.entity.Reservation;
import reservation_service.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByRoomId(Long roomId);

    List<Reservation> findByRoomIdAndStatus(Long roomId, ReservationStatus status);

    List<Reservation> findByMemberIdAndStatus(Long memberId, ReservationStatus status);

    /**
     * Vérifie si une salle est déjà réservée (CONFIRMED) sur un créneau donné.
     * Un créneau chevauche si : début demandé < fin existante ET fin demandée > début existante.
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
           "WHERE r.roomId = :roomId " +
           "AND r.status = 'CONFIRMED' " +
           "AND r.startDateTime < :endDateTime " +
           "AND r.endDateTime > :startDateTime")
    boolean existsConflictingReservation(
            @Param("roomId") Long roomId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * Compte les réservations CONFIRMED actives d'un membre.
     */
    long countByMemberIdAndStatus(Long memberId, ReservationStatus status);
}
