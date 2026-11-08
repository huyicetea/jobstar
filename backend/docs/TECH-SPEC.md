# BE Tech Spec

Shared technical spec for all services. Update here first, then code. Frontend API: `docs/API_SPEC.md`.

## 1. Maven Build

- `backend/pom.xml` (parent, `packaging=pom`): modules `common-lib`, `services/*`.
- Parent imports Spring Boot parent `4.1.x` and BOM `spring-cloud-dependencies:2025.1.2` (Oakwood, supports Boot 4.1).
- groupId `com.jobstar`; artifactId = folder name (`job-service`).
- Services depend on `com.jobstar:common-lib`; never on each other.

## 2. common-lib (`com.jobstar.common`)

- `error`: `ErrorResponse`, `ApiException(code, status)`, `GlobalExceptionHandler`
- `event`: `EventEnvelope<T>`, `OutboxEvent` base entity
- `web`: `CorrelationIdFilter`, `PageResponse<T>`, `CurrentUser` (resolved from gateway headers)
- No business logic, no DB access, no service-specific classes.
- Page format: `{content[], page, size, totalElements, totalPages}`

## 3. Auth & Gateway

- JWT RS256: auth-service signs (private key via `JWT_PRIVATE_KEY_PATH`), publishes `/.well-known/jwks.json`.
- Access token 15 min; refresh token 7 days (random, in Redis, rotated on use).
- Claims: `sub` (userId), `role`, `companyId` (HR only), `jti`.
- Gateway: verifies signature + `exp`, checks `jti` blacklist in Redis, checks role per route.
- Gateway strips any client-sent `X-User-*` headers, then sets `X-User-Id`, `X-User-Role`, `X-Company-Id`, `X-Correlation-Id`.
- Public routes: `/api/v1/auth/register|login|refresh`, `GET /api/v1/jobs` and `/jobs/{id}`. All others need a valid token.

## 4. Service-to-Service REST

- Internal paths use `/internal/v1/...`; the gateway never routes them.
- Base URL from env: `JOB_SERVICE_URL`, `AI_SERVICE_URL`, etc. (Docker DNS names).
- Timeouts: connect 1s, read 3s; Resilience4j circuit breaker on calls to ai-service.
- Forward `X-Correlation-Id`, `X-User-Id`, `X-User-Role` on every call.
- Prefer Kafka read-models (`job_snapshot`) over sync calls.

## 5. Ports

| Service | Port | Infra | Port |
|---------|------|-------|------|
| api-gateway | 8080 | Postgres | 5432 |
| auth-service | 8081 | Redis | 6379 |
| job-service | 8082 | Kafka | 9092 |
| candidate-service | 8083 | MinIO | 9000 / 9001 |
| application-service | 8084 | Qdrant | 6333 |
| notification-service | 8085 | | |
| ai-service (Python) | 8090 | | |

## 6. Environment Variables

- DB: `DB_URL`, `DB_USER`, `DB_PASSWORD` (one DB per service, set per container)
- `REDIS_HOST`, `KAFKA_BOOTSTRAP_SERVERS`, `QDRANT_URL`
- MinIO: `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`
- Auth: `JWT_PRIVATE_KEY_PATH`, `JWT_JWKS_URL` (gateway)
- Local values in `.env` (ignored by git); keys listed in `.env.example`.

## 7. Deployment

- `docker-compose.yml` at repo root: Postgres (5 DBs), Redis, Kafka (KRaft), MinIO, Qdrant, services.
- Each service has a `Dockerfile` and exposes `/actuator/health`.
- `ai-service` lives at repo root `ai-service/` (outside `backend/`); backend only calls its REST API and Kafka topics.
- CI (GitHub Actions): build, test, image build on every PR.
