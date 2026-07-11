# Full-Stack Test — Appointment Scheduling

This project is structured as two top-level modules:

```
.
├── backend/    <- Spring Boot API + Kafka event processing (implemented)
└── frontend/   <- not implemented yet
```

## Backend

Spring Boot backend (hexagonal architecture) with Kafka-based event processing and H2 storage.

This repo has **two branches** implementing the exact same backend use cases with two different architectures:

- **`main`** — monolith, everything in a single Spring Boot app.
- **`feat/multi-module`** — the same domain split into 3 independently deployable Maven modules.

Everything you need — how to run each branch (both the plain "infra-only" mode and the fully-containerized mode), the environment variables, and the trade-offs between the two architectures — is documented in **[`backend/README.md`](backend/README.md)**.

## Frontend

Not implemented in this repository yet.
