# INSTRUCTION.md

## 1. Overview

- Name: jobstar
- Recruitment website with 3 roles: `USER` (candidate), `HR`, `ADMIN`.
- Core feature: AI matching (hybrid dense + BM25 search in Qdrant, re-scored in code) of CVs and jobs.

## 2. Tech Stack

- Frontend: React 19 + Vite + TypeScript (React Router, TanStack Query, Zustand, Tailwind 4)
- Backend: Java 21, Spring Boot 4.1, Spring Cloud Gateway
- AI service: Python 3.11 + FastAPI
- Data: PostgreSQL 16, Redis 7, Kafka (KRaft), MinIO (CV PDF), Qdrant (vectors)
- Infra: Docker Compose, GitHub Actions

## 3. Services

| Service | Owns |
|---------|------|
| api-gateway | Routing, JWT verify, rate limit |
| auth-service | Users, companies, login, HR approval |
| job-service | Jobs, SQL search, expiry scheduler |
| candidate-service | CVs (form + PDF) |
| application-service | Apply, status flow, job snapshot |
| notification-service | Notifications, WebSocket push |
| ai-service | CV parsing, embeddings, recommend, eval |

## 4. Repo Layout

```
backend/services/<service>/  # one Spring Boot app per service
backend/common-lib/          # event envelope, error format, shared web classes
backend/docs/                # ARCHITECTURE.md, PROJECT-RULES.md, TECH-SPEC.md
ai-service/app/              # api, ingestion, parsing, retrieval, explain, skills
frontend/src/                # app/, features/, shared/
docs/                        # INSTRUCTION.md, DATABASE.md, API_SPEC.md
```

## 5. Architecture Principles

- Backend: layer-based inside each service (controller, service, repository, model).
- Frontend: feature-based (`src/features/<name>`); shared code only in `src/shared`.
- Backend shared code only in `common-lib`; services never depend on each other.
- Talk to other services via REST (`/internal/v1`) or Kafka, never via their database.
- Database per service; Postgres is the source of truth, Qdrant is a rebuildable index.
- Gateway is the only entry point; it forwards `X-User-Id`, `X-User-Role`, `X-Company-Id`, `X-Correlation-Id`.
- Events: transactional outbox + idempotent consumers.

## 6. Conventions

- Public REST prefix `/api/v1`; errors use the shared error format from `common-lib`.
- Propagate `correlationId` (HTTP header and Kafka header).
- DB changes only via Flyway migrations.

## 7. Rules for AI Assistants

- Never write code comments.
- Never read or write another service's database.
- Keep each doc file under 80 lines, English, bullet style with examples.
