---
name: explain-concept
description: Explains a technical concept or design pattern with a simple analogy, the problem it solves, and a minimal example tied to jobstar when relevant. Use when asked "what is X" or "explain X" (e.g. outbox, RRF, JWKS, idempotent consumer).
argument-hint: <concept name>
allowed-tools: Read, Grep, Glob
---

## Instructions
Explain the concept in the user's argument. Avoid academic jargon; use a real-world analogy. Focus on the problem it solves. If jobstar uses it, say where (check the docs), otherwise stay generic. Keep it under ~15 lines, in English.

## Steps
1. **Analogy:** One sentence using a simple analogy.
2. **Problem It Solves:** Why it exists; what breaks without it.
3. **Core Mechanics:** 2-3 bullets on how it works.
4. **Minimal Example:** A tiny snippet or diagram.
5. **In jobstar:** Where it is used (doc/file reference), if at all.

## Example
**Input:** `/explain-concept transactional outbox`
**Output:**
- **Analogy:** Write the letter and the "to be posted" note in the same drawer; a clerk posts letters later.
- **Problem:** Saving to the DB and publishing to Kafka are two systems; a crash between them loses or invents events.
- **Mechanics:** Save business row + `outbox` row in one transaction; a relay reads `outbox` and publishes; consumers dedupe by `eventId`.
- **Example:** `jobRepo.save(job); outboxRepo.save(new OutboxEvent("job.created", job.id()));` in one `@Transactional`.
- **In jobstar:** `docs/DATABASE.md` section 2, `backend/docs/PROJECT-RULES.md` section 7.

## References
- Project patterns: `backend/docs/ARCHITECTURE.md`, `backend/docs/TECH-SPEC.md`, `docs/DATABASE.md`.
