# DATABASE.md

## 1. Storage Overview

| Store | Purpose | Owner |
|-------|---------|-------|
| PostgreSQL | Business data (source of truth) | Each service has its own DB |
| MinIO | CV PDF files | `candidate-service` |
| Qdrant | Job/CV vectors (dense + sparse) | `ai-service` |
| Redis | Refresh tokens, blacklist, rate limit, cache | Any service (own key prefix) |
| Kafka | Async events between services | See section 6 |

## 2. Principles

- **Database per service**: no shared tables, no cross-service JOIN or FK.
- Cross-service reference = ID only (UUID). Example: `applications.job_id`.
- Qdrant is derived: it must be rebuildable from Postgres (`reindex` script).
- Write business data + `outbox` row in **one transaction**; a relay publishes to Kafka.

## 3. Naming Conventions

- `snake_case`; tables plural (`job_skills`); PK `id UUID`; FK `<entity>_id`
- Index `idx_<table>_<column>`; unique `uq_<table>_<column>`
- Audit: `created_at`, `updated_at` (`TIMESTAMPTZ`, UTC); enums: `VARCHAR` + check

## 4. Tables per Service

- **auth_db**: `users` (email UNIQUE, full_name, password_hash, role `USER|HR|ADMIN`, status `ACTIVE|PENDING|LOCKED`, company_id), `companies` (name, tax_code, verified)
- **job_db**: `jobs` (company_id, title, description, level, job_type, work_mode, location, salary_min/max, deadline, status `OPEN|CLOSED|DRAFT`, version, deleted_at for soft delete), `job_skills` (job_id, skill, required, must_have), `outbox`
- **candidate_db**: `cvs` (user_id, structured JSONB, file_key, is_primary, searchable, version), `cv_skills`, `cv_experiences`, `outbox`
- **application_db**: `applications` (job_id, cv_id, user_id, status, match_score, applied_at; unique `(user_id, job_id)`), `application_history`, `job_snapshot`, `outbox`
- **notification_db**: `notifications` (user_id, type, data JSONB, read)
- Status flow: `APPLIED -> VIEWED -> SHORTLISTED -> INTERVIEW -> OFFERED -> HIRED`; exits: `REJECTED`, `WITHDRAWN`, `DECLINED`

## 5. Non-Relational Stores

**MinIO**: bucket `cv-files`, key `{userId}/{uuid}.pdf`; PDF only (magic bytes `%PDF`), max 5MB. Store `file_key` only; serve via presigned URL (5 min).

**Qdrant**: collections `jobs`, `candidates`; vectors `dense` (1536, cosine) + `sparse` (BM25, IDF).
- Payload indexes: `status`, `deadline_ts`, `location`, `level`, `required_skills`, `searchable`...
- Always filter `status=OPEN AND deadline_ts >= now` inside the query. No PII in payload.

**Redis**: key `<service>:<feature>:<id>`, always with TTL. Examples: `ai:rec:{cvId}` (10 min), `auth:refresh:{hash}` (7 d), `auth:jti:{jti}` (token's remaining life).

## 6. Kafka Events

| Topic | Producer | Consumer |
|-------|----------|----------|
| `job.events` | job-service | ai-service, application-service |
| `cv.events` | candidate-service | ai-service |
| `application.events` | application-service | notification-service, ai-service |
| `user.events` | auth-service | notification-service, candidate-service |

- Envelope: `{eventId, type, version, occurredAt, producer, correlationId, data}`; key = aggregate id.
- Consumers are idempotent (store `eventId`); failures retry 3x, then `<topic>.DLQ`.

## 7. Migrations & Local Setup

- Flyway per service: `src/main/resources/db/migration/V{n}__{description}.sql`
- Never edit an applied migration; add a new one (nullable column first, backfill, then constrain).
- Docker: one Postgres, DBs `auth_db`, `job_db`, `candidate_db`, `application_db`, `notification_db`.

## 8. Do / Don't

- Do index WHERE/JOIN columns; paginate lists.
- Don't store files in Postgres or query another service's DB.
