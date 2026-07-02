# 00 — Project Context (Backend, Current Status)

> **Purpose:** give anyone (human or AI) enough context to continue backend development from a
> blank session without losing direction. **Update this file whenever a major status changes.**

Last updated: **2026-07-01**

---

## 1. What is this?

The backend REST API for **SPK Mainan** — a **Decision Support System** (Sistem Pendukung
Keputusan) in the **toys** domain. It serves the Flutter app in [`../../MOBILE`](../../MOBILE).
The sibling `FRONTEND/` folder (web) is not started yet.

The concrete SPK domain (criteria, alternatives, weights, scoring method) is **not yet defined**
— it must be decided before building core features. See [05_ROADMAP.md](05_ROADMAP.md).

---

## 2. What already exists ✅

A production-shaped foundation with one complete feature slice: **Auth**.

### Stack & build
Spring Boot 3.4 / Java 21, Maven wrapper (`./mvnw` — no local Maven needed). H2 in-memory by
default (zero setup); PostgreSQL via the `prod` profile. Flyway migrations, JWT auth, Swagger,
Actuator. No Lombok.

### Auth feature (the template to copy)
```
auth/AuthController.java     # POST /auth/login, POST /auth/logout, GET /auth/profile
auth/AuthService.java        # authenticate, issue JWT, load profile
auth/dto/                    # LoginRequest, LoginResponse (records)
user/User.java + Role.java   # entity + role enum
user/UserRepository.java     # Spring Data JPA
user/dto/UserResponse.java   # response DTO matching the mobile client
```

### Cross-cutting
- `security/` — `JwtService`, `JwtAuthenticationFilter`, `AppUserDetails(+Service)`,
  `RestAuthenticationEntryPoint` (JSON 401).
- `common/exception/` — `ApiException` hierarchy + `GlobalExceptionHandler` → single
  `ErrorResponse` shape.
- `config/` — `SecurityConfig` (stateless JWT, CORS, public paths), `OpenApiConfig` (Swagger +
  bearer), `DataInitializer` (seeds the dev admin user).

### Schema
`db/migration/V1__init_users.sql` creates the `users` table. `ddl-auto: validate`.

### Quality gates
`./mvnw test` is green: `AuthControllerTest` (login success / bad password / validation / no-token)
plus a context-load test.

---

## 3. What is mocked / not production-ready ⚠️

- **Dev admin user is auto-seeded** (`admin` / `password123`) by `DataInitializer` — only under
  non-`prod` profiles. There is no self-registration endpoint yet.
- **Default JWT secret** in `application.yml` is a dev placeholder. Production **must** set
  `JWT_SECRET` (and `DB_*`, `CORS_ALLOWED_ORIGINS`).
- **Logout is a no-op** (stateless JWT — the client discards the token). There is no token
  revocation / blacklist.
- **No SPK domain** yet — no criteria/alternatives/scoring endpoints.

---

## 4. Is it "ready to develop features"? 

**Yes.** Architecture, security, error handling, migrations, and Swagger are in place, with Auth
as an end-to-end example. Before/while building the first business feature:

| # | Action | Priority |
|---|---|---|
| 1 | Define the SPK domain in [05_ROADMAP.md](05_ROADMAP.md) (criteria, alternatives, method) | 🔴 High |
| 2 | Build the first feature package following [02_DEVELOPMENT_GUIDE.md](02_DEVELOPMENT_GUIDE.md) | 🔴 High |
| 3 | Point the mobile app at this API (`ApiConfig.baseUrl`) and remove its mock-auth block | 🔴 High |
| 4 | Provision a real PostgreSQL + set prod env vars | 🟡 Medium |
| 5 | Add token revocation only if a real logout/ban requirement appears | 🟢 Low |

---

## 5. How to run

```bash
./mvnw spring-boot:run        # dev, H2, http://localhost:8080/v1
./mvnw test                   # tests
```
Swagger: `http://localhost:8080/v1/swagger-ui.html`. Full details in
[../README.md](../README.md).
