# Appointment Service

Backend service for scheduling appointments, built with Spring Boot (hexagonal architecture), Kafka, and H2. This is a monolith: the REST API, domain logic, Kafka producer and Kafka consumer all run in a single deployable app.

> This project also has a `feat/multi-module` branch with the same use cases split into independently deployable Maven modules (API vs. worker). Check out that branch to see the modular version — this README only covers the monolith on this branch.

## Prerequisites

- Java 25
- Docker (for Kafka, Schema Registry, and Redpanda Console)

## Running locally

Two ways to run it, depending on how much you want containerized:

### Normal mode (infra in Docker, app on your machine)

1. Start the supporting infrastructure (Kafka, Schema Registry, Redpanda Console):

   ```bash
   docker compose up -d
   ```

2. Start the application:

   ```bash
   ./mvnw spring-boot:run
   ```

The API will be available at `http://localhost:8080`.

### Full container mode (everything in Docker)

Builds the app image and starts everything: Kafka, Schema Registry, Redpanda Console, the app, and the observability stack (Loki + Promtail + Grafana reading the app's log file).

```bash
docker compose -f docker-compose-full.yml up -d --build
```

- App: http://localhost:8080
- Redpanda Console (Kafka UI): http://localhost:8090
- Grafana (logs): http://localhost:3000

## API documentation

Swagger UI: http://localhost:8080/swagger-ui.html

## H2 Console

The application uses an in-memory H2 database. While the app is running, you can inspect it from the browser:

1. Open http://localhost:8080/h2-console
2. Fill in the login form with:
   - **Driver Class**: `org.h2.Driver`
   - **JDBC URL**: `jdbc:h2:mem:appointment-db`
   - **User Name**: `sa`
   - **Password**: *(leave blank)*
3. Click **Connect**

> The database is in-memory and is reset every time the application restarts. Schema is managed by Flyway (`src/main/resources/db/migration`).

## Kafka topics

- `appointment-events-topic` — appointment domain events, published on creation
- `appointment-events-topic-dlt` — events that failed to process after retries are exhausted
- `appointment-events-topic-deserialization-dlt` — messages that failed to deserialize (poison pills)

## Kafka configuration

The topic name, dead-letter suffixes, and retry policy are bound from `application.yml` via a type-safe `@ConfigurationProperties(prefix = "appointment.kafka")` class (`AppointmentKafkaProperties`) instead of being hardcoded, so they can be overridden per environment without touching code:

```yaml
appointment:
  kafka:
    topic: appointment-events-topic
    dlt:
      suffix: -dlt
      deserialization-suffix: -deserialization-dlt
    retry:
      backoff-interval-ms: 1000
      max-attempts: 2
```

## Environment variables

Everything has a working default for local development. These are the ones actually consumed by the app (relevant when running in `docker-compose-full.yml` to point at the containerized infra instead of `localhost`):

| Variable | Default (local) | Purpose |
|---|---|---|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `SPRING_KAFKA_PRODUCER_PROPERTIES_SCHEMA_REGISTRY_URL` | `http://localhost:8081` | Avro schema registry, producer side |
| `SPRING_KAFKA_CONSUMER_PROPERTIES_SCHEMA_REGISTRY_URL` | `http://localhost:8081` | Avro schema registry, consumer side |
| `APPOINTMENT_KAFKA_TOPIC` | `appointment-events-topic` | Kafka topic for appointment events |
| `APPOINTMENT_KAFKA_DLT_SUFFIX` | `-dlt` | Suffix for the dead-letter topic |
| `APPOINTMENT_KAFKA_DLT_DESERIALIZATION_SUFFIX` | `-deserialization-dlt` | Suffix for the poison-pill (deserialization failure) topic |
| `APPOINTMENT_KAFKA_RETRY_BACKOFF_INTERVAL_MS` | `1000` | Delay between consumer retry attempts |
| `APPOINTMENT_KAFKA_RETRY_MAX_ATTEMPTS` | `2` | Max consumer retry attempts before publishing to DLT |
