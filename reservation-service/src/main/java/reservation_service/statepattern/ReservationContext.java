package reservation_service.statepattern;

import reservation_service.entity.Reservation;
import reservation_service.entity.ReservationStatus;

/**
 * Contexte du State Pattern.
 *
 * Encapsule une {@link Reservation} et délègue les transitions
 * à l'état courant. Le service n'interagit qu'avec ce contexte,
 * sans jamais connaître les classes d'état concrètes.
 *
 * Usage :
 * <pre>
 *   ReservationContext ctx = new ReservationContext(reservation);
 *   ctx.cancel();   // OK si CONFIRMED, exception si CANCELLED/COMPLETED
 *   ctx.complete(); // OK si CONFIRMED, exception sinon
 * </pre>
 */
public class ReservationContext {

    private final Reservation reservation;
    private ReservationState currentState;

    public ReservationContext(Reservation reservation) {
        this.reservation = reservation;
        this.currentState = resolveState(reservation.getStatus());
    }

    /**
     * Résout l'état courant à partir du statut persisté en base.
     */
    private ReservationState resolveState(ReservationStatus status) {
        return switch (status) {
            case CONFIRMED  -> new ConfirmedState();
            case CANCELLED  -> new CancelledState();
            case COMPLETED  -> new CompletedState();
        };
    }

    /**
     * Déclenche la transition vers CANCELLED.
     * Délègue à l'état courant qui lève une exception si la transition est invalide.
     */
    public void cancel() {
        currentState.cancel(reservation);
        currentState = new CancelledState();
    }

    /**
     * Déclenche la transition vers COMPLETED.
     * Délègue à l'état courant qui lève une exception si la transition est invalide.
     */
    public void complete() {
        currentState.complete(reservation);
        currentState = new CompletedState();
    }

    public Reservation getReservation() {
        return reservation;
    }

    public String getCurrentStateName() {
        return currentState.getStateName();
    }
}
