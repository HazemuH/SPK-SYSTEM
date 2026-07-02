# 05 — Roadmap & Feature Tracker (Backend)

> A living document. **Update it whenever you start/finish a feature** so direction survives across
> sessions. Reflect major status changes in [00_PROJECT_CONTEXT.md](00_PROJECT_CONTEXT.md) too.
> The SPK product decisions live in the mobile roadmap ([`../../MOBILE/docs/05_ROADMAP.md`](../../MOBILE/docs/05_ROADMAP.md))
> — keep the two aligned.

---

## 🎯 Product goal (FILL THIS IN FIRST)

> ⚠️ Not yet defined. Decide before building core features.

- **SPK method:** _(SAW / WP / TOPSIS / AHP — pick one or more)_
- **Core domain entities:**
  - Criteria — name, weight, type (benefit/cost)
  - Alternatives (toys) — the options being scored
  - Scores — value of each alternative per criterion
  - Ranking result — computed output of the chosen method

---

## 🧱 Foundation (done)

- [x] Spring Boot 3.4 / Java 21 project, Maven wrapper
- [x] Package-by-feature layered architecture (controller/service/repository/entity + DTOs)
- [x] JWT auth (stateless, BCrypt) + Spring Security filter chain
- [x] Global exception handling → single `ErrorResponse` shape
- [x] Spring Data JPA + Flyway (`V1__init_users.sql`), `ddl-auto: validate`
- [x] H2 (dev/test) + PostgreSQL (prod) profiles, env-var config
- [x] Swagger UI + Actuator health
- [x] Auth feature slice (login/logout/profile) as a template
- [x] `./mvnw test` green

---

## 🚧 Backlog (prioritize & fill in)

| Priority | Feature | Status | Notes |
|---|---|---|---|
| 🔴 | Define the SPK domain | ⬜ Todo | fill in "Product goal" above |
| 🔴 | Point mobile at this API | ⬜ Todo | set `ApiConfig.baseUrl`, remove mobile mock-auth block |
| 🔴 | First feature: Criteria CRUD | ⬜ Todo | follow Recipe A in [02_DEVELOPMENT_GUIDE.md](02_DEVELOPMENT_GUIDE.md) |
| 🟡 | Alternatives (toys) CRUD | ⬜ Todo | |
| 🟡 | Scores (alternative × criterion) | ⬜ Todo | |
| 🟡 | SPK scoring & ranking endpoint | ⬜ Todo | pure calculation in a service |
| 🟡 | User self-registration / management | ⬜ Todo | if needed beyond the seeded admin |
| 🟢 | Provision real PostgreSQL + prod env | ⬜ Todo | `DB_*`, `JWT_SECRET`, CORS |
| 🟢 | Token revocation / refresh tokens | ⬜ Todo | only if a real requirement appears |

Status legend: ⬜ Todo · 🔄 In Progress · ✅ Done · ⏸️ Blocked

---

## ✅ Done

_(empty — move items here as they ship, with a date)_

---

## 📝 New-feature entry template

```markdown
### Feature: <name>
- **Start date:** YYYY-MM-DD
- **Goal:** what it enables
- **Migration:** V<n>__<desc>.sql
- **Endpoints:** METHOD /path ...
- **Entities / DTOs:** ...
- **Definition of done:** ./mvnw test green ☐ · API_REFERENCE updated ☐ · happy+failure test ☐
- **Notes / decisions:**
```

---

## 🧭 Decision Log

| Date | Decision | Reason |
|---|---|---|
| 2026-07-01 | Spring Boot 3.4 / Java 21, package-by-feature | standard, scalable layout; features stay self-contained |
| 2026-07-01 | Stateless JWT, no server session | simple, mobile-friendly; logout is client-side token discard |
| 2026-07-01 | No Lombok | avoid annotation-processor coupling (JDK 23 installed); plain Java is clear enough |
| 2026-07-01 | DTO records + static `from()` mappers, no mapper lib | least complexity for the project size |
| 2026-07-01 | Flyway owns schema, `ddl-auto: validate` | reproducible, reviewable migrations; no surprise DDL |
| 2026-07-01 | H2 dev default, PostgreSQL prod | zero-setup local run; production-grade DB in prod |
