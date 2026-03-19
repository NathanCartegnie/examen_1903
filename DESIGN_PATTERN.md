# Design Pattern — State Pattern

## Pattern choisi : State (Comportemental)

---

## Contexte

Le microservice `reservation-service` gère le cycle de vie d'une réservation à travers trois statuts :

```
CONFIRMED ──► CANCELLED
CONFIRMED ──► COMPLETED
```

Les états `CANCELLED` et `COMPLETED` sont **terminaux** : aucune transition n'est possible depuis ces états.

---

## Problème sans pattern

Sans pattern, la gestion des transitions repose sur des `if/switch` dans le service :

```java
// Code sans pattern — à ne PAS faire
public void cancel(Reservation r) {
    if (r.getStatus() == CONFIRMED) {
        r.setStatus(CANCELLED);
    } else if (r.getStatus() == CANCELLED) {
        throw new IllegalStateException("Déjà annulée");
    } else if (r.getStatus() == COMPLETED) {
        throw new IllegalStateException("Déjà complétée");
    }
}
```

Ce code devient **ingérable** dès qu'on ajoute un état ou une transition :
- La logique de transition est éparpillée dans le service
- Chaque méthode (`cancel`, `complete`) duplique la même structure conditionnelle
- Ajouter un état `PENDING` ou `EXPIRED` impose de modifier toutes les méthodes existantes

---

## Solution : State Pattern

### Structure mise en place

```
ReservationState          (interface)
├── ConfirmedState        → cancel() ✓  complete() ✓
├── CancelledState        → cancel() ✗  complete() ✗  (état terminal)
└── CompletedState        → cancel() ✗  complete() ✗  (état terminal)

ReservationContext        (façade utilisée par ReservationService)
└── délègue à l'état courant
```

### Fichiers concernés

| Fichier | Rôle |
|---|---|
| `ReservationState.java` | Interface définissant les opérations `cancel()` et `complete()` |
| `ConfirmedState.java` | Implémentation de l'état CONFIRMED (transitions valides) |
| `CancelledState.java` | Implémentation de l'état CANCELLED (état terminal) |
| `CompletedState.java` | Implémentation de l'état COMPLETED (état terminal) |
| `ReservationContext.java` | Contexte du pattern, résout l'état depuis le statut en base |

### Utilisation dans ReservationService

```java
// Annulation via State Pattern
ReservationContext context = new ReservationContext(reservation);
context.cancel(); // délègue à ConfirmedState.cancel() ou lève IllegalStateException

// Complétion via State Pattern
ReservationContext context = new ReservationContext(reservation);
context.complete(); // idem
```

Le `ReservationService` **ne connaît pas** les classes d'état concrètes. Il interagit uniquement avec `ReservationContext`.

---

## Justification du choix

### Pourquoi le State Pattern plutôt que le Builder Pattern ?

Le **Builder Pattern** aurait été pertinent pour la *construction* d'une réservation avec ses multiples validations (disponibilité salle, quota membre, conflit de créneau). Cependant, ces validations sont déjà bien isolées dans `ReservationService.createReservation()` et font appel à des ressources externes (REST, base de données) qui rendent un Builder classique moins adapté.

Le **State Pattern** est le choix le plus naturel ici car :

1. **Le domaine métier le demande explicitement** : les règles de transition (`CONFIRMED → CANCELLED` possible, `CANCELLED → CANCELLED` impossible) sont exactement ce que le State Pattern modélise.

2. **Principe Ouvert/Fermé (OCP)** : ajouter un nouvel état (ex. `PENDING_PAYMENT`) n'impose pas de modifier le code existant. Il suffit de créer une nouvelle classe implémentant `ReservationState`.

3. **Élimination des conditions** : chaque classe d'état porte sa propre logique. Le service n'a plus aucun `if/switch` sur le statut.

4. **Sémantique claire** : `CancelledState.cancel()` qui lève une `IllegalStateException` est auto-documenté. Le comportement interdit est *encodé dans le type*, pas dans une condition.

5. **Cohérence avec le GlobalExceptionHandler** : les `IllegalStateException` levées par les états terminaux sont interceptées et retournées avec un HTTP 409 Conflict, ce qui donne une API REST sémantiquement correcte.

---

## Diagramme UML simplifié

```
         ReservationContext
              │
              │ uses
              ▼
       «interface»
      ReservationState
       ┌──────────┐
       │ cancel() │
       │complete()│
       └──────────┘
            ▲
     ┌──────┼──────┐
     │      │      │
ConfirmedState  CancelledState  CompletedState
  cancel() ✓    cancel() ✗      cancel() ✗
 complete() ✓  complete() ✗    complete() ✗
```
