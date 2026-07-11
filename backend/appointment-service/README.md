# Appointment Service

Serviço de backend para agendamento de consultas, construído com Spring Boot (arquitetura hexagonal), Kafka e H2. É um monólito: a API REST, a lógica de domínio, o produtor Kafka e o consumidor Kafka rodam todos em uma única aplicação implantável.

> Este projeto também tem uma branch `feat/multi-module` com os mesmos casos de uso divididos em módulos Maven implantáveis de forma independente (API vs. worker). Veja essa branch para a versão modular — este README cobre apenas o monólito desta branch.

## Pré-requisitos

- Java 25
- Docker (para Kafka, Schema Registry e Redpanda Console)

## Rodando localmente

Duas formas de rodar, dependendo do quanto você quer containerizado:

### Modo normal (infra no Docker, app na sua máquina)

1. Suba a infraestrutura de suporte (Kafka, Schema Registry, Redpanda Console):

   ```bash
   docker compose up -d
   ```

2. Suba a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

A API ficará disponível em `http://localhost:8080`.

### Modo totalmente containerizado (tudo no Docker)

Builda a imagem da aplicação e sobe tudo: Kafka, Schema Registry, Redpanda Console, a aplicação e a stack de observabilidade (Loki + Promtail + Grafana lendo o arquivo de log da aplicação).

```bash
docker compose -f docker-compose-full.yml up -d --build
```

- App: http://localhost:8080
- Redpanda Console (UI do Kafka): http://localhost:8090
- Grafana (logs): http://localhost:3000

## Documentação da API

Swagger UI: http://localhost:8080/swagger-ui.html

## Console H2

A aplicação usa um banco H2 em memória. Enquanto a aplicação estiver rodando, você pode inspecioná-lo pelo navegador:

1. Abra http://localhost:8080/h2-console
2. Preencha o formulário de login com:
   - **Driver Class**: `org.h2.Driver`
   - **JDBC URL**: `jdbc:h2:mem:appointment-db`
   - **User Name**: `sa`
   - **Password**: *(deixe em branco)*
3. Clique em **Connect**

> O banco é em memória e é resetado a cada reinício da aplicação. O schema é gerenciado pelo Flyway (`src/main/resources/db/migration`).

## Tópicos Kafka

- `appointment-events-topic` — eventos de domínio de agendamento, publicados na criação
- `appointment-events-topic-dlt` — eventos que falharam ao processar após esgotarem as tentativas
- `appointment-events-topic-deserialization-dlt` — mensagens que falharam na deserialização (poison pills)

## Configuração do Kafka

O nome do tópico, os sufixos de dead-letter e a política de retry vêm do `application.yml` através de uma classe type-safe `@ConfigurationProperties(prefix = "appointment.kafka")` (`AppointmentKafkaProperties`), em vez de ficarem hardcoded, então podem ser sobrescritos por ambiente sem tocar no código:

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

## Variáveis de ambiente

Tudo tem um valor padrão funcional para desenvolvimento local. Estas são as que a aplicação realmente consome (relevantes ao rodar com `docker-compose-full.yml` para apontar para a infra containerizada em vez de `localhost`):

| Variável | Padrão (local) | Finalidade |
|---|---|---|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Endereço do broker Kafka |
| `SPRING_KAFKA_PRODUCER_PROPERTIES_SCHEMA_REGISTRY_URL` | `http://localhost:8081` | Schema registry Avro, lado produtor |
| `SPRING_KAFKA_CONSUMER_PROPERTIES_SCHEMA_REGISTRY_URL` | `http://localhost:8081` | Schema registry Avro, lado consumidor |
| `APPOINTMENT_KAFKA_TOPIC` | `appointment-events-topic` | Tópico Kafka para eventos de agendamento |
| `APPOINTMENT_KAFKA_DLT_SUFFIX` | `-dlt` | Sufixo do tópico de dead-letter |
| `APPOINTMENT_KAFKA_DLT_DESERIALIZATION_SUFFIX` | `-deserialization-dlt` | Sufixo do tópico de poison-pill (falha de deserialização) |
| `APPOINTMENT_KAFKA_RETRY_BACKOFF_INTERVAL_MS` | `1000` | Intervalo entre tentativas de retry do consumidor |
| `APPOINTMENT_KAFKA_RETRY_MAX_ATTEMPTS` | `2` | Número máximo de tentativas de retry do consumidor antes de publicar na DLT |
