package reservation_service.statepattern;

import reservation_service.entity.Reservation;

/**
 * État CANCELLED : état terminal.
 * Aucune transition autorisée depuis cet état.
 */
public class CancelledState implements ReservationState {

    @Override
    public void cancel(Reservation reservation) {
        throw new IllegalStateException(
                "Impossible d'annuler une réservation déjà annulée (id=" + reservation.getId() + ")");
    }

    @Override
    public void complete(Reservation reservation) {
        throw new IllegalStateException(
                "Impossible de compléter une réservation annulée (id=" + reservation.getId() + ")");
    }

    @Override
    public String getStateName() {
        return "CANCELLED";
    }
}
