package room_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RoomEvents {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomDeletedEvent {
        private Long roomId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationAvailabilityEvent {
        private Long roomId;
        private boolean available;
    }
}
