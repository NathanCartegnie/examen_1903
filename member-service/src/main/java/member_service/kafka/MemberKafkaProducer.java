package member_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMemberDeletedEvent(Long memberId) {
        var event = new MemberEvents.MemberDeletedEvent(memberId);
        log.info("Publication événement member-deleted pour le membre {}", memberId);
        kafkaTemplate.send("member-deleted", String.valueOf(memberId), event);
    }
}
