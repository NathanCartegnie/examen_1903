package reservation_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /** Notifie le Room Service de mettre à jour la disponibilité. */
    public void sendRoomAvailabilityEvent(Long roomId, boolean available) {
        var event = new ReservationEvents.RoomAvailabilityEvent(roomId, available);
        log.info("Publication room-availability-updated : salle {} → disponible={}", roomId, available);
        kafkaTemplate.send("room-availability-updated", String.valueOf(roomId), event);
    }

    /** Notifie le Member Service de mettre à jour la suspension. */
    public void sendMemberSuspensionEvent(Long memberId, boolean suspended) {
        var event = new ReservationEvents.MemberSuspensionEvent(memberId, suspended);
        log.info("Publication member-suspension-updated : membre {} → suspendu={}", memberId, suspended);
        kafkaTemplate.send("member-suspension-updated", String.valueOf(memberId), event);
    }
}
