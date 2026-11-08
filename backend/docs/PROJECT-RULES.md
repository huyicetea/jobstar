# BE Project Rules

Rules for every Spring Boot service in **jobstar**. Keep it short; update when conventions change.
See `ARCHITECTURE.md` (design) and `TECH-SPEC.md` (build, JWT, ports, env vars).

## 1. Stack & Layout

- Java 21, Spring Boot 4.1.x, Maven; one `pom.xml` per service.
- Services live in `backend/services/<service>/`; shared code only in `backend/common-lib/`.
- Base package `com.jobstar.<service>` (e.g. `com.jobstar.job`).

## 2. Package Layout

- `model/entity`, `model/enums`, `model/dto/request`, `model/dto/response`
- `controller`, `service`, `repository`; optional `security`, `config`, `exception`, `mapper`
- Example: `com.jobstar.job.controller.JobController`

## 3. Layer Rules

- Controller: HTTP only (validate, call service, return DTO); no business logic, no repository calls.
- Service: business logic + `@Transactional`; one public method = one use case.
- Repository: Spring Data JPA only; no business rules.
- Never return entities from controllers; map to response DTOs.
- Never read or write another service's database; use REST or Kafka.

## 4. Naming

- Classes `PascalCase`: `JobService`, `CreateJobRequest`, `JobResponse`, `JobNotFoundException`.
- Methods `camelCase`, verb first: `createJob`, `findOpenJobs`.
- Tables/columns follow DATABASE.md (`snake_case`, plural tables, UUID PK).

## 5. API Rules

- Prefix `/api/v1`; plural nouns: `/api/v1/jobs/{id}`.
- Lists are paginated: `page` (0-based), `size` (max 50), `sort=field,asc|desc`.
- Validate input with Jakarta Validation (`@Valid`, `@NotBlank`, `@Size`).
- Identity comes from gateway headers `X-User-Id`, `X-User-Role`, `X-Company-Id`; never from the body.
- Authorize by role and ownership (HR edits only jobs of own `companyId`).

## 6. Errors

- Throw domain exceptions; one `@RestControllerAdvice` maps them to the shared format in `common-lib`.
- Format: `{timestamp, status, code, message, path, correlationId, details[]}`.
- Codes (see `docs/API_SPEC.md`): `VALIDATION_FAILED`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `RATE_LIMITED`.
- Never leak stack traces or SQL errors.

## 7. Data & Events

- Schema changes only via Flyway: `src/main/resources/db/migration/V{n}__{description}.sql`; never edit an applied one.
- `spring.jpa.hibernate.ddl-auto=validate`; index WHERE/JOIN columns; avoid N+1 (`@EntityGraph`).
- Business row + `outbox` row in one transaction; relay publishes to Kafka.
- Envelope: `{eventId, type, version, occurredAt, producer, correlationId, data}`; key = aggregate id.
- Consumers are idempotent (store `eventId`); retry 3x, then `<topic>.DLQ`.

## 8. Logging

- SLF4J only; no `System.out`; never log passwords, tokens, CV content, or PII.
- `correlationId` in MDC; propagate via `X-Correlation-Id` and Kafka header.

## 9. Code Style

- Never write code comments; use clear names and small methods.
- Constructor injection only; no field `@Autowired`.
- Use `Optional`/exceptions instead of `null`; prefer records for DTOs.

## 10. Testing & Git

- Unit tests: JUnit 5 + Mockito; controller slices: `@WebMvcTest`; integration: Testcontainers.
- Tests live in `src/test/java` (Maven standard).
- Conventional commits: `feat(job): add expiry scheduler`.
- Never add Claude as contributor or co-author; never commit secrets (use env vars, `.env.example`).
