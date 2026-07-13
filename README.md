# Full-Stack Test — Agendamento de Consultas

Este projeto está estruturado em dois módulos de alto nível:

```
.
├── backend/    <- API Spring Boot + processamento de eventos Kafka (implementado)
└── frontend/   <- cadastro Verity em 3 etapas, React + TypeScript + Vite (implementado)
```

## Backend

Esta é a branch **monólito** (`main`): a API REST, a lógica de domínio, o produtor Kafka e o consumidor Kafka rodam juntos como uma única aplicação Spring Boot em `backend/appointment-service`.

Existe uma branch irmã, **`feat/multi-module`**, com os mesmos casos de uso divididos em módulos Maven implantáveis de forma independente (API vs. worker). Veja essa branch para a versão modular — ela tem seu próprio README.

Para saber como rodar esta branch (tanto no modo "infra no Docker, app local" quanto totalmente containerizado), variáveis de ambiente e tudo mais, veja **[`backend/appointment-service/README.md`](backend/appointment-service/README.md)**.

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
