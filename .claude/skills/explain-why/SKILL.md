---
name: explain-why
description: Explains the rationale and trade-offs behind a technical or architectural decision in jobstar. Use when asked "why did we choose X", "why X instead of Y", or to justify a design.
argument-hint: <decision, e.g. Qdrant instead of pgvector>
allowed-tools: Read, Grep, Glob
---

## Instructions
Explain the decision in the user's argument. Take rationale only from the docs and code (`docs/`, `backend/docs/`); there are no decision records. Anything you infer must be labeled "(inferred)". Never invent history. Stay objective and acknowledge drawbacks. Keep it under ~15 lines, in English.

## Steps
1. **Context:** The requirement or constraint behind the decision.
2. **Decision:** What was chosen.
3. **Trade-offs:** 2 pros, 2 cons.
4. **Rejected Alternatives:** 1-2 options and why they lose.
5. **Source:** Doc/file where the decision is recorded, or "not documented".

## Example
**Input:** `/explain-why Qdrant instead of pgvector`
**Output:**
- **Context:** Job matching needs dense + BM25 hybrid search with a hard filter (`status=OPEN`, `deadline_ts >= now`) applied inside the query.
- **Decision:** Qdrant with named `dense` + `sparse` vectors, fused with RRF.
- **Trade-offs:** *Pros:* native hybrid search and payload-filtered HNSW. *Cons:* one more service to run; must be kept in sync with Postgres (outbox + `reindex`).
- **Rejected (inferred):** pgvector, which has no built-in BM25 or RRF fusion; Elasticsearch, heavier to operate for a solo team.
- **Source:** `backend/docs/ARCHITECTURE.md` section 6.

## References
- `backend/docs/ARCHITECTURE.md`, `backend/docs/TECH-SPEC.md`, `docs/DATABASE.md`.
