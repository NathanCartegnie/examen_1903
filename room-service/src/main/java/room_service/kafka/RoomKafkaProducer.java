package room_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendRoomDeletedEvent(Long roomId) {
        var event = new RoomEvents.RoomDeletedEvent(roomId);
        log.info("Publication événement room-deleted pour la salle {}", roomId);
        kafkaTemplate.send(KafkaTopics.ROOM_DELETED, String.valueOf(roomId), event);
    }
}
