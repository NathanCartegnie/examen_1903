package member_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MemberEvents {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberDeletedEvent {
        private Long memberId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSuspensionEvent {
        private Long memberId;
        private boolean suspended;
    }
}
