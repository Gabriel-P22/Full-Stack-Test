# Backend — Appointment Scheduling

Spring Boot backend (hexagonal architecture) for scheduling appointments, with Kafka-based event processing and H2 storage. This repository has **two branches** implementing the same use cases with two different architectural approaches. This README covers both, how to run each locally, and the trade-offs between them.

## Prerequisites

- Java 25
- Docker + Docker Compose

## Branches

| Branch | Architecture | Root module |
|---|---|---|
| `main` | Monolith — one Spring Boot app | `backend/appointment-service` |
| `feat/multi-module` | Multi-module — 3 Maven modules | `backend/` (aggregator) |

### `main` — Monolith

A single Spring Boot application owns the REST API, domain logic, the Kafka producer (publishes appointment events), and the Kafka consumer (confirms/cancels appointments from those events).

```
backend/
  appointment-service/   <- everything: API, domain, producer, consumer
```

### `feat/multi-module` — Multi-module

The same domain split into three independently buildable/deployable Maven modules:

```
backend/
  appointment-common/    <- shared domain entities, JPA persistence, Avro/Kafka DTOs, Flyway migrations
  appointment-service/   <- REST API + Kafka producer only
  appointment-worker/    <- standalone Kafka consumer only
```

`appointment-service` and `appointment-worker` both depend on `appointment-common` and can be built, deployed, and scaled independently.

## Advantages / trade-offs

| | Monolith (`main`) | Multi-module (`feat/multi-module`) |
|---|---|---|
| **Setup complexity** | Lower — one module, one `pom.xml`, one deployable | Higher — 3 modules, shared parent POM, inter-module dependency management |
| **Deployability** | API and event-consumer always ship and scale together | API and worker deploy/scale independently (e.g. scale worker replicas without touching the API) |
| **Blast radius** | A bug or crash in Kafka consumption can affect the same process serving HTTP traffic | Consumer crash is isolated to the worker process; API stays up |
| **Build time / CI** | Single build, fast | Slightly slower (multi-module reactor build), but incremental builds only rebuild changed modules |
| **Code reuse** | N/A, everything is local | Domain/entities/migrations defined once in `appointment-common`, no duplication between API and worker |
| **Best fit** | Small team, low traffic, simplicity is priority | Independent scaling of read (API) vs. event-processing (worker) paths matters |

## Configuration (`appointment.kafka.*`)

Both branches externalize the Kafka topic name, dead-letter-topic suffixes, and retry policy via a type-safe `@ConfigurationProperties(prefix = "appointment.kafka")` class instead of hardcoding them, so they can be overridden per environment without code changes:

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

Every key above can also be overridden via environment variable (Spring's relaxed binding), e.g. `APPOINTMENT_KAFKA_TOPIC`, `APPOINTMENT_KAFKA_RETRY_MAX_ATTEMPTS`.

## Environment variables

These are the variables actually consumed by the apps (used to point at infra when running fully containerized — see below). Everything else has a working default in `application.yml` for local development.

| Variable | Used by | Default (local) | Purpose |
|---|---|---|---|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | service, worker | `localhost:9092` | Kafka broker address |
| `SPRING_KAFKA_PRODUCER_PROPERTIES_SCHEMA_REGISTRY_URL` | service, worker | `http://localhost:8081` | Avro schema registry, producer side |
| `SPRING_KAFKA_CONSUMER_PROPERTIES_SCHEMA_REGISTRY_URL` | service (monolith)/worker (multi-module) | `http://localhost:8081` | Avro schema registry, consumer side |
| `APPOINTMENT_KAFKA_TOPIC` | service, worker | `appointment-events-topic` | Kafka topic for appointment events |
| `APPOINTMENT_KAFKA_DLT_SUFFIX` | worker | `-dlt` | Suffix for the dead-letter topic |
| `APPOINTMENT_KAFKA_DLT_DESERIALIZATION_SUFFIX` | worker | `-deserialization-dlt` | Suffix for the poison-pill (deserialization failure) topic |
| `APPOINTMENT_KAFKA_RETRY_BACKOFF_INTERVAL_MS` | worker | `1000` | Delay between consumer retry attempts |
| `APPOINTMENT_KAFKA_RETRY_MAX_ATTEMPTS` | worker | `2` | Max consumer retry attempts before publishing to DLT |

## Running locally

Each branch ships two Docker Compose files:

- **`docker-compose.yml`** ("normal") — infra only (Kafka, Schema Registry, Redpanda Console). You run the Spring Boot app(s) yourself, outside Docker, so you can attach a debugger / get fast reload.
- **`docker-compose-full.yml`** ("full") — infra **plus** the app(s) built and run as containers, plus the observability stack (Loki + Promtail + Grafana) reading the apps' log files. Closest to a real deployment; slower to iterate on.

### Monolith (`main`) — normal mode

```bash
cd backend/appointment-service

# 1. Start infra only
docker compose up -d

# 2. Run the app locally
./mvnw spring-boot:run
```

API: http://localhost:8080 · Swagger: http://localhost:8080/swagger-ui.html

### Monolith (`main`) — full container mode

```bash
cd backend/appointment-service
docker compose -f docker-compose-full.yml up -d --build
```

This builds the app image and starts everything: Kafka, Schema Registry, Redpanda Console (http://localhost:8090), the app (http://localhost:8080), Loki, Promtail and Grafana (http://localhost:3000).

### Multi-module (`feat/multi-module`) — normal mode

```bash
git checkout feat/multi-module
cd backend

# 1. Start infra only
docker compose up -d

# 2. Run each module locally, in separate terminals
./mvnw -pl appointment-service -am spring-boot:run
./mvnw -pl appointment-worker -am spring-boot:run
```

API: http://localhost:8080 · Swagger: http://localhost:8080/swagger-ui.html. The worker has no HTTP surface — it just consumes from Kafka.

### Multi-module (`feat/multi-module`) — full container mode

```bash
git checkout feat/multi-module
cd backend
docker compose -f docker-compose-full.yml up -d --build
```

Builds both the `app` (API) and `worker` images via the multi-module Maven reactor and starts the full stack: Kafka, Schema Registry, Redpanda Console, the API (http://localhost:8080), the worker, Loki, Promtail and Grafana (http://localhost:3000).

## Tests & coverage

Both branches enforce a 90% line-coverage gate via JaCoCo in CI:

```bash
./mvnw verify
```

Run from `backend/appointment-service` on `main`, or from `backend/` (builds all 3 modules) on `feat/multi-module`.
