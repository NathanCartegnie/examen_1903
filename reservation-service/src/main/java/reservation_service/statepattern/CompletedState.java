package reservation_service.statepattern;

import reservation_service.entity.Reservation;

/**
 * État COMPLETED : état terminal.
 * Aucune transition autorisée depuis cet état.
 */
public class CompletedState implements ReservationState {

    @Override
    public void cancel(Reservation reservation) {
        throw new IllegalStateException(
                "Impossible d'annuler une réservation déjà complétée (id=" + reservation.getId() + ")");
    }

    @Override
    public void complete(Reservation reservation) {
        throw new IllegalStateException(
                "Impossible de compléter une réservation déjà complétée (id=" + reservation.getId() + ")");
    }

    @Override
    public String getStateName() {
        return "COMPLETED";
    }
}
