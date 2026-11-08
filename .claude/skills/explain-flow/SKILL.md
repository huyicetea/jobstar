---
name: explain-flow
description: Traces a feature or request end to end, from entry point through gateway, services, database and Kafka back to the response. Use when asked "how does X work", "what happens when X", or to debug a request path in jobstar.
argument-hint: <feature or endpoint, e.g. apply to job>
allowed-tools: Read, Grep, Glob
---

## Instructions
Trace the flow named in the user's argument, strictly in order. Use the docs for the designed flow and the code for what is actually implemented; mark steps that are designed but not yet coded. Highlight async hops. Keep it under ~20 lines, in English.

## Steps
1. **Trigger:** Exact route, scheduler or UI action.
2. **Execution Path:** Numbered steps: gateway (JWT, headers) -> Controller -> Service -> Repository -> DB / other service.
3. **Side Effects:** Outbox row, Kafka topic, Redis cache, notifications, WebSocket push.
4. **Final Output:** Status code and response shape.

## Example
**Input:** `/explain-flow apply to job`
**Output:**
- **Trigger:** `POST /api/v1/applications` `{jobId, cvId}` by a `USER`.
- **Path:**
  1. Gateway verifies JWT, sets `X-User-Id`, `X-User-Role`.
  2. `ApplicationController` validates the request.
  3. `ApplicationService` checks `job_snapshot` is `OPEN` and not past deadline, and `(user_id, job_id)` is unique.
  4. Saves `applications` + `outbox(application.created)` in one transaction.
- **Side Effects (async):** Relay publishes `application.events` -> notification-service stores a notification and pushes it to the HR over WebSocket.
- **Output:** `201 Created` with the application.

## References
- Endpoints: `docs/API_SPEC.md`; design: `backend/docs/ARCHITECTURE.md`; events: `docs/DATABASE.md`.
