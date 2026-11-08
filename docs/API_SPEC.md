# API Spec

REST contract of **jobstar** via api-gateway. Keep it short; update when endpoints change.

## 1. Conventions

- Prefix `/api/v1`; JSON; UTC ISO-8601 times; ids are UUID.
- Auth: `Authorization: Bearer <accessToken>`; gateway adds `X-User-Id`, `X-User-Role`, `X-Company-Id`.
- Header `X-Correlation-Id` accepted and echoed on every response.
- Pagination: `?page=0&size=20&sort=createdAt,desc` (size max 50).
- Page response: `{content: [], page, size, totalElements, totalPages}`.
- Error: `{timestamp, status, code, message, path, correlationId, details[]}`.
- Codes: `VALIDATION_FAILED` 400, `UNAUTHORIZED` 401, `FORBIDDEN` 403, `NOT_FOUND` 404, `CONFLICT` 409, `RATE_LIMITED` 429.

## 2. Auth (auth-service)

| Method | Path | Role | Note |
|--------|------|------|------|
| POST | `/auth/register` | Public | Body `{email, password, fullName, role: USER or HR, company?}`; HR starts `PENDING` |
| POST | `/auth/login` | Public | Returns `{accessToken, refreshToken, user}` |
| POST | `/auth/refresh` | Public | Body `{refreshToken}` |
| POST | `/auth/logout` | Any | Blacklists token |
| GET | `/users/me` | Any | Current user |
| GET | `/admin/pending-hr` | ADMIN | Pending HR list |
| PATCH | `/admin/approve-hr/{userId}` | ADMIN | Body `{approved: true}` |

## 3. Jobs (job-service)

| Method | Path | Role | Note |
|--------|------|------|------|
| GET | `/jobs` | Public | Filters: `keyword, location, level, jobType, workMode, salaryMin, salaryMax, skills` |
| GET | `/jobs/{id}` | Public | Job detail |
| POST | `/jobs` | HR | Create job |
| PUT | `/jobs/{id}` | HR (owner) | Update job |
| PATCH | `/jobs/{id}/status` | HR (owner) | `OPEN` or `CLOSED` |
| DELETE | `/jobs/{id}` | HR (owner) | Soft delete |
| GET | `/hr/jobs` | HR | Own company jobs |

## 4. Candidate (candidate-service)

| Method | Path | Role | Note |
|--------|------|------|------|
| GET | `/cvs` | USER | Own CVs |
| POST | `/cvs` | USER | Create CV from form |
| PUT | `/cvs/{id}` | USER | Update CV |
| DELETE | `/cvs/{id}` | USER | Delete CV |
| POST | `/cvs/upload` | USER | `multipart/form-data` field `file`, PDF max 5MB |
| GET | `/cvs/{id}/file-url` | USER, HR | Presigned URL (5 min) |
| PATCH | `/cvs/{id}/primary` | USER | Set primary |
| PATCH | `/cvs/{id}/searchable` | USER | Body `{searchable: true}` |

## 5. Applications (application-service)

| Method | Path | Role | Note |
|--------|------|------|------|
| POST | `/applications` | USER | Body `{jobId, cvId}`; one per `(user, job)` |
| GET | `/applications/me` | USER | Own applications |
| PATCH | `/applications/{id}/withdraw` | USER | Status `WITHDRAWN` |
| PATCH | `/applications/{id}/decline` | USER | After `OFFERED` |
| GET | `/hr/jobs/{jobId}/applications` | HR (owner) | Applicants with `matchScore` |
| PATCH | `/applications/{id}/status` | HR (owner) | Body `{status, note?}` per status flow |

## 6. AI (ai-service)

| Method | Path | Role | Note |
|--------|------|------|------|
| GET | `/recommendations/jobs?cvId=` | USER | Jobs ranked with `matchScore`, `matchedSkills`, `missingSkills` |
| GET | `/recommendations/candidates?jobId=` | HR | Candidates with `searchable=true` |
| POST | `/ai/cv/parse` | USER | Parse uploaded PDF to structured CV |

## 7. Notifications (notification-service)

| Method | Path | Role | Note |
|--------|------|------|------|
| GET | `/notifications` | Any | Paginated |
| GET | `/notifications/unread-count` | Any | `{count}` |
| PATCH | `/notifications/{id}/read` | Any | Mark read |

- WebSocket: `/ws/notifications?token=<accessToken>`; message `{id, type, data, createdAt}`.