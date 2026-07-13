# Verity — Cadastro (Teste Técnico Front-End)

Formulário de cadastro em 3 etapas (Dados Pessoais, Informações Residenciais e
Informações Profissionais) com resumo final e exportação em PDF, construído
com React + TypeScript + Vite.

## Stack

- **React 19** + **TypeScript** + **Vite**
- **React Hook Form** + **Zod** para validação dos formulários
- **react-router-dom** para navegação entre etapas
- **react-imask** para máscaras de Data de Nascimento, CPF, Telefone e CEP
- **jsPDF** para exportação do resumo em PDF
- **json-server** como serviço mockado para busca de CEP e lista de profissões
- **Vitest** + **Testing Library** para os testes unitários (cobertura ≥ 80%)

## Como rodar

Instale as dependências (pnpm):

```bash
pnpm install
```

Suba a aplicação e a API mockada juntas:

```bash
pnpm dev:all
```

Isso inicia o Vite em `http://localhost:5173` e o json-server em
`http://localhost:3001` (dados em `mock/db.json`, com os endpoints
`GET /ceps/:cep` e `GET /professions`).

Se preferir rodar cada um separadamente:

```bash
pnpm dev        # front-end
pnpm mock:api   # API mockada
```

## Busca de CEP

Ao digitar um CEP completo, a aplicação primeiro consulta o json-server
(`/ceps/:cep`). Caso o CEP não exista no mock, ela cai automaticamente para o
ViaCEP (endpoint público dos Correios/ViaCEP) como fallback — cobrindo tanto o
requisito principal quanto o "plus" do desafio. CEPs de exemplo disponíveis no
mock: `01310-100`, `01001-000`, `20040-020`, `30130-010`, `80010-000`,
`90010-000`, `40010-000`, `70040-010`, `60060-170`, `50030-230`.

## Testes

```bash
pnpm test            # roda a suíte uma vez
pnpm test:watch      # modo watch
pnpm test:coverage   # com relatório de cobertura (mínimo configurado: 80%)
```

## Build

```bash
pnpm build
pnpm preview
```

## Identidade visual

A paleta (azul/branco) e o wordmark "verity" usados são um placeholder até a
logo oficial da Verity ser fornecida — o componente `Logo`
(`src/components/layout/Logo.tsx`) foi isolado exatamente para facilitar essa
troca depois.
