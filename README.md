# Job Hunter API

Backend da plataforma **Job Hunter**, responsavel por centralizar vagas, perfis profissionais, compatibilidade, candidaturas, notificacoes e integracoes com coletores.

## Status atual

Este repositorio foi evoluido a partir de uma API existente de ingestao e busca de vagas. A **Fase 1 (Fundacao)** esta implementada:

- Projeto Spring Boot 3.4.x com Java 17 e Maven.
- PostgreSQL como banco oficial.
- Flyway com migration versionada.
- Estrutura modular por dominio em `br.com.jobhunter`.
- Respostas padronizadas de sucesso e erro.
- Tratamento global de excecoes com `@RestControllerAdvice`.
- Spring Security configurado como base (sessao stateless, CORS, BCrypt e handlers JSON).
- Documentacao OpenAPI/Swagger.
- Dockerfile e Docker Compose com PostgreSQL, healthchecks e volume persistente.
- Testes de integracao com Testcontainers/PostgreSQL.
- Modulo de vagas existente preservado: cadastro, pesquisa, paginacao, ingestao em lote, deduplicacao e integracao com Telegram.

Funcionalidades das fases seguintes ainda **nao** estao implementadas: autenticacao JWT completa, CRUD de usuarios, perfil profissional, upload de curriculo, matching, candidaturas, notificacoes FCM, automacao Playwright e endpoints `/api/v1/integrations/jobs`.

## Tecnologias

- Java 17
- Spring Boot 3.4.5
- Spring Web, Spring Security, Spring Data JPA, Bean Validation
- PostgreSQL 16
- Flyway
- Springdoc OpenAPI 2.8.6
- Lombok
- JUnit 5, Mockito e Testcontainers
- Docker e Docker Compose

## Estrutura

```text
src/main/java/br/com/jobhunter/
├── JobHunterApplication.java
├── common/
│   ├── exception/       # excecoes de negocio e handler global
│   └── response/        # ApiResponse, ErrorResponse e ApiError
├── config/              # seguranca, CORS, OpenAPI e filtro de ingestao
├── job/
│   ├── dto/
│   ├── JobController.java
│   ├── Job.java
│   ├── JobRepository.java
│   ├── JobService.java
│   ├── JobSpecifications.java
│   └── JobFingerprint.java
└── notification/
    └── telegram/        # servico e webhook existentes
```

Os modulos `auth`, `user`, `profile`, `source`, `matching`, `application`, `automation` e `integration` serao adicionados nas fases seguintes.

## Como executar

### Docker Compose (recomendado)

```bash
cp .env.example .env
docker compose up --build
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`
- PostgreSQL: `localhost:5432`

### Execucao local

Suba apenas o PostgreSQL:

```bash
docker compose up -d postgres
```

Depois execute a aplicacao:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=jobhunter
export DB_USERNAME=jobhunter
export DB_PASSWORD=jobhunter
mvn spring-boot:run
```

## Variaveis de ambiente

| Variavel | Descricao | Padrao |
| --- | --- | --- |
| `DB_HOST` | Host do PostgreSQL | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_NAME` | Nome do banco | `jobhunter` |
| `DB_USERNAME` | Usuario do banco | `jobhunter` |
| `DB_PASSWORD` | Senha do banco | `jobhunter` |
| `PORT` | Porta HTTP da API | `8080` |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas, separadas por virgula | `http://localhost:4200,http://localhost:8081` |
| `INGEST_TOKEN` | Token dos coletores para ingestao | `change-me-ingest-token` |
| `JWT_SECRET` | Segredo JWT (reservado para a Fase 2) | - |
| `JWT_ACCESS_EXPIRATION` | Expiracao do access token em segundos (Fase 2) | `900` |
| `TELEGRAM_ENABLED` | Habilita envio via Telegram | `false` |
| `TELEGRAM_BOT_TOKEN` | Token do bot Telegram | - |
| `TELEGRAM_CHAT_ID` | Chat de destino | - |
| `TELEGRAM_WEBHOOK_SECRET` | Segredo do webhook do Telegram | - |
| `FIREBASE_CREDENTIALS_PATH` | Caminho das credenciais FCM (Fase 5) | - |

Nunca versione o arquivo `.env`. Use `.env.example` como referencia.

## Endpoints disponiveis

Todos os endpoints de vagas aceitam `/api/v1/jobs` e o caminho legado `/api/jobs`.

| Metodo | Endpoint | Descricao |
| --- | --- | --- |
| `GET` | `/api/v1/jobs` | Pesquisa com filtros e paginacao |
| `GET` | `/api/v1/jobs/{id}` | Detalhes da vaga |
| `GET` | `/api/v1/jobs/stats` | Total de vagas por fonte |
| `POST` | `/api/v1/jobs` | Cadastro manual de vaga |
| `PUT` | `/api/v1/jobs/{id}` | Atualizacao de vaga |
| `DELETE` | `/api/v1/jobs/{id}` | Remocao de vaga |
| `POST` | `/api/v1/jobs/ingest` | Ingestao em lote de vagas normalizadas |
| `POST` | `/api/telegram/webhook` | Recebimento de vagas via Telegram |

Filtros de pesquisa: `q`, `company`, `location`, `source`, `remote`, `page`, `size`, `sort`.

A ingestao exige o header `X-Ingest-Token` ou `Authorization: Bearer <token>`.

## Formato das respostas

Sucesso:

```json
{
  "success": true,
  "message": "Operacao realizada com sucesso",
  "data": {},
  "timestamp": "2026-11-05T12:00:00Z"
}
```

Erro:

```json
{
  "success": false,
  "message": "Erro de validacao",
  "errors": [
    {
      "field": "title",
      "message": "must not be blank"
    }
  ],
  "timestamp": "2026-11-05T12:00:00Z"
}
```

## Banco de dados

O schema e controlado exclusivamente pelo Flyway. O Hibernate usa `ddl-auto: validate` e nunca altera o schema automaticamente.

Migration atual:

- `V1__create_jobs_table.sql`: tabela `jobs`, indices e restricoes de unicidade por `fingerprint` e por `(source, external_id)`.

Datas sao persistidas em UTC (`TIMESTAMP WITH TIME ZONE` no PostgreSQL e `hibernate.jdbc.time_zone=UTC`).

## Testes

```bash
mvn test
```

Os testes de integracao usam Testcontainers e exigem Docker em execucao. Eles validam criacao, pesquisa, caminho legado, validacao, 404, autenticacao da ingestao e deduplicacao por `external_id`/fingerprint.

## Decisoes arquiteturais

- O pacote foi migrado de `com.jobhunter.api` para `br.com.jobhunter`, conforme a estrutura alvo.
- SQLite foi substituido por PostgreSQL; a migration `V1` foi reescrita para sintaxe PostgreSQL porque nao havia dados de producao versionados.
- O caminho legado `/api/jobs` foi mantido como alias para preservar clientes existentes enquanto a API passa a usar `/api/v1/jobs`.
- A seguranca da Fase 1 e um esqueleto: sessao stateless, CORS, BCrypt e respostas JSON para 401/403. As regras JWT/RBAC entram na Fase 2.
- O filtro de token de ingestao continua independente do Spring Security e compara tokens em tempo constante.
- A ingestao preserva deduplicacao por `(source, externalId)` e por fingerprint normalizado de titulo, empresa e localizacao.

## Proximas fases

1. **Fase 2**: usuarios, cadastro, login, JWT, refresh/revogacao, RBAC e testes de autorizacao.
2. **Fase 3**: entidades completas de fonte/vaga, modalidade, situacao, coletores Python e `/api/v1/integrations/jobs`.
3. **Fase 4**: perfil profissional, curriculo e matching.
4. **Fase 5**: candidaturas, notificacoes Telegram/FCM e preferencias.
5. **Fase 6**: automacao Playwright com aprovacao explicita.
