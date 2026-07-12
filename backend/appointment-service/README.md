# Appointment Service

Backend service for scheduling appointments, built with Spring Boot (hexagonal architecture), Kafka, and H2.

> This module is part of a multi-module Maven build (`appointment-common`, `appointment-service`, `appointment-worker`). See the [root README](../README.md) for the full picture, including how `appointment-worker` fits in and how the two share a database.

## Prerequisites

- Java 25
- Docker (for Kafka, Schema Registry, and Redpanda Console)

## Running locally

1. Start the supporting infrastructure (Kafka, Schema Registry, Redpanda Console) from the repo root:

   ```bash
   docker compose -f docker-compose.yml up -d
   ```

2. Start the application (from the repo root, so the multi-module build resolves correctly):

   ```bash
   ./mvnw -pl appointment-service spring-boot:run
   ```

The API will be available at `http://localhost:8080`.

## API documentation

Swagger UI: http://localhost:8080/swagger-ui/index.html

## H2 Console

The application uses a **file-based** H2 database, shared with `appointment-worker` via `AUTO_SERVER=TRUE` (not in-memory — see the root README for why). While the app is running, you can inspect it from the browser:

1. Open http://localhost:8080/h2-console
2. Fill in the login form with:
   - **Driver Class**: `org.h2.Driver`
   - **JDBC URL**: `jdbc:h2:file:../data/appointment-db;AUTO_SERVER=TRUE`
   - **User Name**: `sa`
   - **Password**: *(leave blank)*
3. Click **Connect**

> `AUTO_SERVER=TRUE` is required — without it, H2 tries to open the file exclusively and fails with `Database may be already in use`, since the running app already holds it open.

Schema is managed by Flyway (`src/main/resources/db/migration`). To reset the database, stop both `appointment-service` and `appointment-worker` and delete the `backend/data/` directory.

## Kafka topics

- `appointment-events-topic` — appointment domain events, published on creation
- `appointment-events-topic-dlt` — events that failed to process after retries are exhausted
- `appointment-events-topic-deserialization-dlt` — messages that failed to deserialize (poison pills)

Redpanda Console (Kafka UI): http://localhost:8090
