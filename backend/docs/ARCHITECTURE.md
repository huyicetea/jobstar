# BE Architecture

Backend design of **jobstar**. Keep it short; update when services change. Technical spec: `TECH-SPEC.md`. API: `docs/API_SPEC.md`.

## 1. Overview

- Microservices, layer-based inside each service, under `backend/services/<service>/`.
- Gateway is the only entry point; the browser never calls services directly.
- Sync = REST inside the Docker network; async = Kafka events.

## 2. Services

| Service | Owns | DB |
|---------|------|----|
| api-gateway | Routing, JWT verify, rate limit | Redis |
| auth-service | Users, companies, login, HR approval | `auth_db` |
| job-service | Jobs, SQL search, expiry scheduler | `job_db` |
| candidate-service | CVs (form + PDF in MinIO) | `candidate_db` |
| application-service | Apply, status flow, job snapshot | `application_db` |
| notification-service | Notifications, WebSocket push | `notification_db` |
| ai-service (Python, repo root) | CV parsing, embeddings, recommend, eval | Qdrant |

## 3. Request Flow

- Client sends `Authorization: Bearer <jwt>` to the gateway.
- Gateway verifies JWT, rate limits (Redis), forwards identity and correlation headers.
- Services trust these headers only from the gateway; they never parse JWT.
- Controller -> service -> repository; errors map to the shared error format.

## 4. Service Layout (example: `job-service`)

```
job-service/
    src/
        main/java/com/jobstar/job/
            controller/  service/  repository/
            model/{entity,enums,dto/{request,response}}/
            security/ config/ exception/ mapper/ scheduler/   (optional)
            JobServiceApplication.java
        main/resources/{db/migration/, application.yml}
        test/java/
    Dockerfile
    pom.xml
```

## 5. Events

| Topic | Producer | Consumer |
|-------|----------|----------|
| `job.events` | job-service | ai-service, application-service |
| `cv.events` | candidate-service | ai-service |
| `application.events` | application-service | notification-service, ai-service |
| `user.events` | auth-service | notification-service, candidate-service |

- Transactional outbox; idempotent consumers; retry 3x then `<topic>.DLQ` (details in PROJECT-RULES).

## 6. AI Matching

- ai-service: hybrid search in Qdrant (`dense` 1536 cosine + `sparse` BM25).
- Always filter `status=OPEN AND deadline_ts >= now` inside the query.
- Re-score in code (skills, level, location, salary) to get `matchScore`.
- Qdrant is rebuildable from Postgres; no PII in payload. Cache `ai:rec:{cvId}` in Redis, TTL 10 min.

## 7. Application Status Flow

- `APPLIED -> VIEWED -> SHORTLISTED -> INTERVIEW -> OFFERED -> HIRED`
- Exits: `REJECTED`, `WITHDRAWN`, `DECLINED`.
- Each change writes `application_history` and emits `application.events`.

## 8. Security

- Roles `USER`, `HR`, `ADMIN`; HR needs ADMIN approval (`PENDING` -> `ACTIVE`).
- Passwords BCrypt; refresh tokens and blacklist in Redis with TTL.
- CV files: PDF only (`%PDF`), max 5MB, presigned URL (5 min).
