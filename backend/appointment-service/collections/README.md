# Collections

Requisições prontas pra API do `appointment-service`.

## Arquivos

- `curl-requests.sh` — os requests como comandos `curl` avulsos, prontos pra rodar direto no terminal ou
  colar em qualquer cliente HTTP (Postman/Insomnia/Bruno aceitam importar um comando `curl` diretamente).

## Rodando via terminal

```bash
chmod +x curl-requests.sh
./curl-requests.sh    # ou copie/rode um comando de cada vez
```

Requer a aplicação rodando (`./mvnw spring-boot:run` dentro de `backend/appointment-service` — não precisa
de Docker). Troque `<APPOINTMENT_ID>` pelo `id` retornado na criação antes de rodar os requests que
dependem dele.
