package reservation_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReservationEvents {

    /** Publié vers room-service pour libérer ou bloquer une salle. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomAvailabilityEvent {
        private Long roomId;
        private boolean available;
    }

    /** Publié vers member-service pour suspendre ou désuspendre un membre. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSuspensionEvent {
        private Long memberId;
        private boolean suspended;
    }

    /** Consommé depuis room-service : annuler toutes les résa de cette salle. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomDeletedEvent {
        private Long roomId;
    }

    /** Consommé depuis member-service : supprimer toutes les résa de ce membre. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberDeletedEvent {
        private Long memberId;
    }
}
