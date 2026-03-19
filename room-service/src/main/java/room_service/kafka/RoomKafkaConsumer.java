package room_service.kafka;

import room_service.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consomme les événements émis par le Reservation Service pour mettre à jour
 * la disponibilité d'une salle (réservation créée → indisponible,
 * réservation terminée/annulée → disponible).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RoomKafkaConsumer {

    private final RoomService roomService;

    @KafkaListener(topics = "room-availability-updated", groupId = "room-service-group")
    public void handleRoomAvailabilityUpdate(RoomEvents.ReservationAvailabilityEvent event) {
        log.info("Réception événement disponibilité salle {} → {}", event.getRoomId(), event.isAvailable());
        roomService.updateAvailability(event.getRoomId(), event.isAvailable());
    }
}
