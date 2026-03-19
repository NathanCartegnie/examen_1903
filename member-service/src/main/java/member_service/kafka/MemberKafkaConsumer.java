package member_service.kafka;

import member_service.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consomme les événements de suspension/désuspension émis par le Reservation Service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberKafkaConsumer {

    private final MemberService memberService;

    @KafkaListener(topics = "member-suspension-updated", groupId = "member-service-group")
    public void handleMemberSuspension(MemberEvents.MemberSuspensionEvent event) {
        log.info("Réception événement suspension membre {} → {}", event.getMemberId(), event.isSuspended());
        memberService.updateSuspension(event.getMemberId(), event.isSuspended());
    }
}
