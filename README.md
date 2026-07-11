# Full-Stack Test — Agendamento de Consultas

Este projeto está estruturado em dois módulos de alto nível:

```
.
├── backend/    <- API Spring Boot + processamento de eventos Kafka (implementado)
└── frontend/   <- ainda não implementado
```

## Backend

Esta é a branch **monólito** (`main`): a API REST, a lógica de domínio, o produtor Kafka e o consumidor Kafka rodam juntos como uma única aplicação Spring Boot em `backend/appointment-service`.

Existe uma branch irmã, **`feat/multi-module`**, com os mesmos casos de uso divididos em módulos Maven implantáveis de forma independente (API vs. worker). Veja essa branch para a versão modular — ela tem seu próprio README.

Para saber como rodar esta branch (tanto no modo "infra no Docker, app local" quanto totalmente containerizado), variáveis de ambiente e tudo mais, veja **[`backend/appointment-service/README.md`](backend/appointment-service/README.md)**.

## Frontend

Ainda não implementado neste repositório.
