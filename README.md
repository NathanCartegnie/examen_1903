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

### 1. Lancer Kafka avec Docker Compose

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
