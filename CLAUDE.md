# jobstar

Recruitment website. `backend/` (Spring Boot microservices), `frontend/` (React + Vite + TS), `ai-service/` (Python, not created yet).
Ignore `backend_old/` and `frontend_old/`.

## Read before working

- Backend: `backend/docs/ARCHITECTURE.md`, `PROJECT-RULES.md`, `TECH-SPEC.md` (build, JWT, ports, env vars)
- API between frontend and backend: `docs/API_SPEC.md`
- Database, schema, Kafka events: `docs/DATABASE.md`
- General overview: `docs/INSTRUCTION.md`

## Hard rules

- Never write code comments.
- Never read or write another service's database; use REST or Kafka.
- Layer-based packages inside each backend service (controller, service, repository, model).
- Never commit secrets; use env vars and `.env.example`.
- Never add Claude as contributor or co-author in commits or the repo.

## Commands (placeholders until poms exist)

- Backend: `mvn -q verify` (per service or from `backend/`)
- Frontend: `npm run dev | build | lint` in `frontend/`
- Infra: `docker compose up -d`

## Doc upkeep

- Update the matching doc when schema, events, ports or conventions change.
- Keep each doc under 80 lines.
