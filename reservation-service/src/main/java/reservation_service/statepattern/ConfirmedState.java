package reservation_service.statepattern;

import reservation_service.entity.Reservation;
import reservation_service.entity.ReservationStatus;

/**
 * État CONFIRMED : état initial après création.
 * Transitions autorisées : → CANCELLED, → COMPLETED
 */
public class ConfirmedState implements ReservationState {

    @Override
    public void cancel(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELLED);
    }

    @Override
    public void complete(Reservation reservation) {
        reservation.setStatus(ReservationStatus.COMPLETED);
    }

    @Override
    public String getStateName() {
        return "CONFIRMED";
    }
}
