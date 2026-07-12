# Appointment Service

Serviço de agendamento de consultas, construído com Spring Boot (arquitetura hexagonal), Kafka e H2.

Este módulo faz parte de um build Maven multi-módulo (`appointment-common`, `appointment-service`, `appointment-worker`). Veja o [README raiz](../README.md) para a visão completa, incluindo como o `appointment-worker` se encaixa e como os dois compartilham o banco de dados.

## Pré-requisitos

- Java 25
- Docker (para Kafka, Schema Registry e Redpanda Console)

## Rodando localmente

1. Suba a infraestrutura de suporte (Kafka, Schema Registry, Redpanda Console) a partir da raiz do repositório:

   ```bash
   docker compose -f docker-compose.yml up -d
   ```

2. Suba a aplicação (a partir da raiz do repositório, pra resolver o build multi-módulo corretamente):

   ```bash
   ./mvnw -pl appointment-service spring-boot:run
   ```

A API fica disponível em `http://localhost:8080`.

## Documentação da API

Swagger UI: http://localhost:8080/swagger-ui/index.html

## H2 Console

A aplicação usa um banco H2 **em arquivo**, compartilhado com o `appointment-worker` via `AUTO_SERVER=TRUE` (não é em memória). Com a aplicação rodando, dá pra inspecionar o banco pelo navegador:

1. Abra http://localhost:8080/h2-console
2. Preencha o formulário de login com:
   - **Driver Class**: `org.h2.Driver`
   - **JDBC URL**: `jdbc:h2:file:../data/appointment-db;AUTO_SERVER=TRUE`
   - **User Name**: `sa`
   - **Password**: *(deixe em branco)*
3. Clique em **Connect**

O `AUTO_SERVER=TRUE` é obrigatório — sem ele, o H2 tenta abrir o arquivo em modo exclusivo e falha com `Database may be already in use`, já que a aplicação rodando já está com ele aberto.

O schema é gerenciado por Flyway (`src/main/resources/db/migration`). Pra resetar o banco, pare `appointment-service` e `appointment-worker` e apague o diretório `backend/data/`.

## Tópicos Kafka

- `appointment-events-topic` — eventos de domínio do agendamento, publicados na criação
- `appointment-events-topic-dlt` — eventos que falharam ao processar após esgotar as tentativas
- `appointment-events-topic-deserialization-dlt` — mensagens que falharam ao deserializar (poison pills)

Redpanda Console (UI do Kafka): http://localhost:8090
