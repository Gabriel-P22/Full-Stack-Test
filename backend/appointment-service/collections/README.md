# Collections

Requisições prontas pra API do `appointment-service`.

## Arquivos

- `appointment-service.postman_collection.json` — coleção do Postman (schema v2.1), organizada em pastas ("Appointments" e "Error scenarios"), com variáveis `{{baseUrl}}` e `{{appointmentId}}`.
- `curl-requests.sh` — os mesmos requests como comandos `curl` avulsos, prontos pra rodar direto no terminal ou colar em qualquer cliente HTTP.

## Importando no Postman

1. Postman → **Import** → arraste `appointment-service.postman_collection.json` (ou selecione o arquivo).
2. Depois de importar, configure a variável de collection `baseUrl` se a API não estiver em `http://localhost:8080/api/v1` (ex: outra porta local).
3. Rode "Create appointment" primeiro, copie o `id` da resposta e cole na variável `appointmentId` (aba **Variables** da collection) pra usar nos outros requests.

## Importando no Insomnia / Bruno / outros

A maioria dos clientes HTTP aceita colar um comando `curl` diretamente e converte pra um request (Insomnia: **Import > From Clipboard**; Bruno: **Import > cURL**). Basta copiar qualquer bloco de `curl-requests.sh`.

## Rodando via terminal

```bash
chmod +x curl-requests.sh
./curl-requests.sh    # ou copie/rode um comando de cada vez
```

Requer a aplicação rodando (`./mvnw spring-boot:run` dentro de `backend/appointment-service`, ou via `docker-compose.yml` pro Kafka + Schema Registry). Troque `<APPOINTMENT_ID>` pelo `id` retornado na criação antes de rodar os requests que dependem dele.
