# Appointment Service

Backend service for scheduling appointments, built with Spring Boot (hexagonal architecture), Kafka, and H2.

## Prerequisites

- Java 25
- Docker (for Kafka, Schema Registry, and Redpanda Console)

## Running locally

1. Start the supporting infrastructure (Kafka, Schema Registry, Redpanda Console):

   ```bash
   docker-compose up -d
   ```

2. Start the application:

   ```bash
   ./mvnw spring-boot:run
   ```

The API will be available at `http://localhost:8080`.

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

Redpanda Console (Kafka UI): http://localhost:8090

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
