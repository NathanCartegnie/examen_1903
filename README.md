# Coworking Platform — Architecture Microservices

Plateforme de réservation de salles de coworking construite avec Spring Boot, Spring Cloud et Apache Kafka.

---

## Prérequis

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (pour Kafka)

---

## Structure du projet

```
coworking-platform/
├── pom.xml                  ← Parent Maven
├── config-server/           ← Port 8888 — Configuration centralisée
├── discovery-server/        ← Port 8761 — Eureka
├── api-gateway/             ← Port 8080 — Point d'entrée unique
├── room-service/            ← Port 8081 — Gestion des salles
├── member-service/          ← Port 8082 — Gestion des membres
├── reservation-service/     ← Port 8083 — Gestion des réservations
├── DESIGN_PATTERN.md        ← Justification du State Pattern
└── README.md
```

---

## Lancement

### 1. Démarrer Kafka avec Docker

Créez un fichier `docker-compose.yml` à la racine :

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: true
```

Puis lancez :

```bash
docker-compose up -d
```

### 2. Compiler le projet

```bash
cd coworking-platform
mvn clean install -DskipTests
```

### 3. Démarrer les services dans l'ordre

Ouvrez un terminal par service et lancez dans cet ordre :

```bash
# Terminal 1 — Config Server (doit démarrer en premier)
cd config-server
mvn spring-boot:run

# Terminal 2 — Eureka (attend le Config Server)
cd discovery-server
mvn spring-boot:run

# Terminal 3 — API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 4 — Room Service
cd room-service
mvn spring-boot:run

# Terminal 5 — Member Service
cd member-service
mvn spring-boot:run

# Terminal 6 — Reservation Service
cd reservation-service
mvn spring-boot:run
```

> Attendez ~15 secondes entre chaque démarrage pour laisser le temps à Eureka d'enregistrer les services.

---

## URLs utiles

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Config Server | http://localhost:8888 |
| Swagger Room Service | http://localhost:8081/swagger-ui.html |
| Swagger Member Service | http://localhost:8082/swagger-ui.html |
| Swagger Reservation Service | http://localhost:8083/swagger-ui.html |
| H2 Console Room | http://localhost:8081/h2-console |
| H2 Console Member | http://localhost:8082/h2-console |
| H2 Console Reservation | http://localhost:8083/h2-console |

---

## Scénarios de test Postman

### 1. Créer des salles

```
POST http://localhost:8080/api/rooms
Content-Type: application/json

{
  "name": "Salle Alpha",
  "city": "Paris",
  "capacity": 10,
  "type": "MEETING_ROOM",
  "hourlyRate": 25.00
}
```

```
POST http://localhost:8080/api/rooms
{
  "name": "Open Space Beta",
  "city": "Lyon",
  "capacity": 30,
  "type": "OPEN_SPACE",
  "hourlyRate": 15.00
}
```

### 2. Inscrire des membres

```
POST http://localhost:8080/api/members
{
  "fullName": "Alice Martin",
  "email": "alice@example.com",
  "subscriptionType": "BASIC"
}
```

```
POST http://localhost:8080/api/members
{
  "fullName": "Bob Dupont",
  "email": "bob@example.com",
  "subscriptionType": "PRO"
}
```

### 3. Créer une réservation

```
POST http://localhost:8080/api/reservations
{
  "roomId": 1,
  "memberId": 1,
  "startDateTime": "2025-06-01T09:00:00",
  "endDateTime": "2025-06-01T11:00:00"
}
```

✅ La salle 1 passe à `available: false`

### 4. Tenter une réservation en conflit de créneau

```
POST http://localhost:8080/api/reservations
{
  "roomId": 1,
  "memberId": 2,
  "startDateTime": "2025-06-01T10:00:00",
  "endDateTime": "2025-06-01T12:00:00"
}
```

❌ Réponse 400 : "La salle 1 est déjà réservée sur ce créneau"

### 5. Atteindre le quota BASIC (2 réservations)

Créer une 2e réservation pour Alice (memberId=1) sur une autre salle.
Après la 2e réservation, vérifier que `suspended: true` sur Alice.

```
GET http://localhost:8080/api/members/1
```

### 6. Annuler une réservation

```
PATCH http://localhost:8080/api/reservations/1/cancel
```

✅ La réservation passe à `CANCELLED`, la salle redevient disponible, Alice est désuspendue.

### 7. Tenter d'annuler une réservation déjà annulée (State Pattern)

```
PATCH http://localhost:8080/api/reservations/1/cancel
```

❌ Réponse 409 Conflict : "Impossible d'annuler une réservation déjà annulée"

### 8. Supprimer une salle (propagation Kafka)

```
DELETE http://localhost:8080/api/rooms/1
```

✅ Toutes les réservations CONFIRMED de la salle 1 passent à `CANCELLED` via Kafka.

### 9. Supprimer un membre (propagation Kafka)

```
DELETE http://localhost:8080/api/members/1
```

✅ Toutes les réservations d'Alice sont supprimées via Kafka.

---

## Topics Kafka

| Topic | Publié par | Consommé par | Déclencheur |
|---|---|---|---|
| `room-deleted` | Room Service | Reservation Service | Suppression d'une salle |
| `member-deleted` | Member Service | Reservation Service | Suppression d'un membre |
| `room-availability-updated` | Reservation Service | Room Service | Création/annulation/complétion d'une réservation |
| `member-suspension-updated` | Reservation Service | Member Service | Quota atteint ou libéré |

---

## Design Pattern

Voir [DESIGN_PATTERN.md](./DESIGN_PATTERN.md) pour la justification complète du **State Pattern** implémenté dans le `reservation-service`.
