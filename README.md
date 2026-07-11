# Full-Stack Test — Appointment Scheduling

This project is structured as two top-level modules:

```
.
├── backend/    <- Spring Boot API + Kafka event processing (implemented)
└── frontend/   <- not implemented yet
```

## Backend

This is the **monolith** branch (`feat/finish`): the REST API, domain logic, Kafka producer and Kafka consumer all run as a single Spring Boot app under `backend/appointment-service`.

There's a sibling branch, **`feat/multi-module`**, with the exact same use cases split into independently deployable Maven modules (API vs. worker). Check out that branch for the modular version — it has its own README.

For how to run this branch (both a plain "infra in Docker, app local" mode and a fully-containerized mode), environment variables, and everything else, see **[`backend/appointment-service/README.md`](backend/appointment-service/README.md)**.

## Frontend

Not implemented in this repository yet.
