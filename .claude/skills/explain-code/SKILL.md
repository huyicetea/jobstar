---
name: explain-code
description: Explains what a file, class or function does, its data flow and notable logic, without line-by-line translation. Use when asked "what does this file/class/method do" or to explain code in the jobstar repo.
argument-hint: <file path, class or function>
allowed-tools: Read, Grep, Glob
---

## Instructions
Explain the code named in the user's argument. Read it first; never guess. Focus on the "why" and the high-level "how", never line-by-line. Keep the answer under ~15 lines, in English, scannable bullets.

## Steps
1. **Purpose:** One sentence on the main goal of the file/snippet.
2. **Data Flow:** Inputs -> transformations -> outputs.
3. **Key Components:** Main classes/methods and their single responsibility.
4. **Notable Logic:** Edge cases, security implications, transactions, events emitted, or rule violations against project docs.

## Example
**Input:** `/explain-code backend/services/job-service/.../service/JobService.java`
**Output:**
- **Purpose:** Business logic for creating, updating and closing jobs.
- **Data Flow:** `CreateJobRequest` -> validate deadline and salary range -> save `Job` + `outbox` row in one transaction -> return `JobResponse`.
- **Key Components:** `createJob()` (use case), `closeJob()` (ownership check by `companyId`).
- **Notable Logic:** Outbox row guarantees `job.events` is published even if Kafka is down.

## References
- Conventions: `backend/docs/PROJECT-RULES.md`; design: `backend/docs/ARCHITECTURE.md`.
