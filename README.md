# Full-Stack Test — Agendamento de Consultas

Este projeto está estruturado em dois módulos de alto nível:

```
.
├── backend/    <- API Spring Boot (arquitetura hexagonal)
└── frontend/   <- cadastro Verity em 3 etapas, React + TypeScript + Vite (implementado)
```

## Backend

API REST em Spring Boot (`backend/appointment-service`) para gestão de agendamentos, seguindo os
princípios de Clean/Hexagonal Architecture. O pacote raiz `com.desafio.agendamento` é organizado em
camadas:

```
com.desafio.agendamento/
├── entities/            entidades de domínio — sem dependências externas
├── usecases/
│   ├── ports/in/         interfaces dos casos de uso
│   ├── ports/out/        portas de saída (ex: persistência)
│   └── impl/             implementações dos casos de uso
├── adapters/
│   ├── in/controller/    REST controller + DTOs
│   └── out/persistence/  entidade JPA + Spring Data repository + implementação do port
└── frameworks/
    ├── spring/           classe main
    ├── config/           beans de configuração
    └── exceptions/       exceções de domínio + handler global
```

Persistência com Spring Data JPA + H2 em memória (schema versionado via Flyway), validação com Bean
Validation, tratamento de erros centralizado com `@RestControllerAdvice`, documentação via SpringDoc
OpenAPI e testes unitários + de integração.

Para instruções de como rodar a aplicação, veja
**[`backend/appointment-service/README.md`](backend/appointment-service/README.md)**.

## Frontend

Aplicação **Verity** de cadastro em 3 etapas (Dados Pessoais, Informações Residenciais e Informações Profissionais) com resumo final e exportação em PDF, construída com React + TypeScript + Vite.

Principais pontos:

- **React Hook Form + Zod** para validação, com máscaras (react-imask) em Data de Nascimento, CPF, Telefone, CEP e Salário
- Busca automática de CEP via **json-server** mockado, com fallback para o **ViaCEP** público
- Lista de Profissões carregada via GET no json-server
- Dados persistidos em `localStorage` entre as etapas e após reload
- Exportação do resumo em **PDF** (jsPDF)
- Responsivo, com testes unitários (Vitest + Testing Library) cobrindo ≥ 80% do código

Para instruções de setup, scripts disponíveis e detalhes de arquitetura, veja **[`frontend/README.md`](frontend/README.md)**.
