# Appointment Service — Backend

Sistema de agendamento de consultas, em arquitetura hexagonal, dividido em três módulos Maven:

- **`appointment-common`** — código compartilhado: entidades de domínio, schemas Avro, contratos de mensageria Kafka, configuração de validação.
- **`appointment-service`** — API REST (Spring Web + HATEOAS), cria/consulta agendamentos e publica eventos no Kafka.
- **`appointment-worker`** — consumidor Kafka, processa os eventos publicados pelo service (confirmação, retry, dead-letter).

## Pré-requisitos

- Java 25
- Docker (para Kafka, Schema Registry e Redpanda Console)

## Rodando localmente

1. Suba a infraestrutura (Kafka, Schema Registry, Redpanda Console):

   ```bash
   docker compose -f docker-compose.yml up -d
   ```

2. Compile todos os módulos a partir da raiz:

   ```bash
   ./mvnw clean install
   ```

3. Suba o `appointment-service` (porta 8080):

   ```bash
   ./mvnw -pl appointment-service spring-boot:run
   ```

4. Em outro terminal, suba o `appointment-worker`:

   ```bash
   ./mvnw -pl appointment-worker spring-boot:run
   ```

A API fica disponível em `http://localhost:8080`.

> Se algum dos containers de infra for reiniciado, `appointment-service` e `appointment-worker` reconectam sozinhos ao Kafka — não é necessário reiniciá-los junto.

## API

- Todos os endpoints ficam sob `/api/v1`.
- `POST /api/v1/appointment` exige o header `Idempotency-Key` (qualquer string única por tentativa de criação).
- Endpoints: criar, listar (paginado, com filtro por `status`), buscar por id, atualizar status (`PENDING` / `CONFIRMED` / `CANCELED`).

### Swagger UI

`http://localhost:8080/swagger-ui/index.html`

O spec OpenAPI fica em `http://localhost:8080/v3/api-docs`. Os campos de exemplo do request body já vêm preenchidos com valores válidos.

## Banco de dados (H2 compartilhado)

`appointment-service` e `appointment-worker` rodam como processos Java separados, mas usam o **mesmo arquivo H2** (não bancos em memória isolados), porque o worker precisa enxergar os agendamentos criados pelo service:

```yaml
spring.datasource.url: jdbc:h2:file:../data/appointment-db;AUTO_SERVER=TRUE
```

O arquivo fica em `backend/data/appointment-db.mv.db` (ignorado no git). Pra resetar o banco do zero, pare os dois processos e apague a pasta `data/`.

### H2 Console

1. Abra `http://localhost:8080/h2-console` (com o `appointment-service` rodando).
2. Preencha:
   - **Driver Class**: `org.h2.Driver`
   - **JDBC URL**: `jdbc:h2:file:../data/appointment-db;AUTO_SERVER=TRUE`
   - **User Name**: `sa`
   - **Password**: *(vazio)*
3. Clique em **Connect**.

> O `;AUTO_SERVER=TRUE` é obrigatório — sem ele, o H2 tenta abrir o arquivo em modo exclusivo e dá erro `Database may be already in use`, porque o `appointment-service` já está com ele aberto.

Schema gerenciado por Flyway (`appointment-service/src/main/resources/db/migration`).

## Kafka

- `appointment-events-topic` — eventos de agendamento, publicados na criação
- `appointment-events-topic-dlt` — eventos que falharam após esgotar as tentativas de retry
- `appointment-events-topic-deserialization-dlt` — mensagens que falharam ao deserializar (poison pills)

Redpanda Console (UI do Kafka): `http://localhost:8090`
Schema Registry: `http://localhost:8081`

## Rodando tudo via Docker

`docker-compose-full.yml` sobe a infra completa, os dois módulos empacotados (`Dockerfile` em cada um) e observabilidade (Loki + Promtail + Grafana):

```bash
docker compose -f docker-compose-full.yml up -d --build
```

Grafana: `http://localhost:3000`

## Testes

```bash
./mvnw test
```

## Coleções de teste prontas

Veja `collections/README.md` — inclui uma collection do Postman e um script `curl-requests.sh` com todos os endpoints, incluindo cenários de erro.
