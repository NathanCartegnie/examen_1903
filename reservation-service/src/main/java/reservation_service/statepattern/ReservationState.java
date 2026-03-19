package reservation_service.statepattern;

import reservation_service.entity.Reservation;

/**
 * Interface du State Pattern pour le cycle de vie d'une réservation.
 *
 * Chaque état concret (ConfirmedState, CancelledState, CompletedState)
 * implémente les transitions autorisées et lève une exception pour
 * les transitions interdites.
 *
 * Cycle de vie :
 *   CONFIRMED ──► CANCELLED
 *   CONFIRMED ──► COMPLETED
 *   CANCELLED et COMPLETED sont des états terminaux (aucune transition possible)
 */
public interface ReservationState {

    /**
     * Annule la réservation.
     * @param reservation l'entité à modifier
     * @throws IllegalStateException si la transition est interdite
     */
    void cancel(Reservation reservation);

    /**
     * Marque la réservation comme complétée.
     * @param reservation l'entité à modifier
     * @throws IllegalStateException si la transition est interdite
     */
    void complete(Reservation reservation);

    /**
     * Retourne le nom de l'état courant pour les logs et messages d'erreur.
     */
    String getStateName();
}
