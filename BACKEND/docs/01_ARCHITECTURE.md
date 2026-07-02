# 01 — Architecture

Layered, **package-by-feature** Spring Boot service. Read [CLAUDE.md](../CLAUDE.md) first — it
holds the strict rules; this doc explains the *why* and the request flow.

---

## Layers

Within a feature, dependencies point one direction. A layer never reaches "up".

```
HTTP request
   │
   ▼
Controller  ── validates DTO, no logic ──────────────┐
   │  (DTO in / DTO out)                              │  throws propagate up to
   ▼                                                  │  GlobalExceptionHandler
Service     ── business logic, @Transactional ────────┤  (@RestControllerAdvice)
   │                                                  │
   ▼                                                  │
Repository  ── Spring Data JPA, query methods ────────┘
   │
   ▼
Entity (JPA)  ↔  Database (Flyway-managed schema)
```

- **Controller** (`@RestController`): route mapping, `@Valid` on request DTOs, calls one service,
  returns a response DTO wrapped in `ResponseEntity`. Never touches repositories or entities.
- **Service** (`@Service`): the only place with business logic. Owns transactions
  (`@Transactional`, `readOnly = true` for pure reads). Converts entities to DTOs. Throws
  `ApiException` subclasses for expected failures.
- **Repository** (`interface extends JpaRepository`): persistence. Derived query methods
  (`findByUsername`) or `@Query`. No logic.
- **Entity** (`@Entity`): the persistence model. Stays out of the web layer entirely.

### Why entities never leave the service layer
Returning entities from controllers couples the JSON contract to the DB schema, risks lazy-loading
serialization errors, and can leak fields (e.g. `password`). DTOs give a stable, intentional
contract — the one the mobile app depends on.

---

## Cross-cutting concerns

| Concern | Where | Notes |
|---|---|---|
| Security / JWT | `security/` + `config/SecurityConfig` | stateless filter chain |
| Error rendering | `common/exception/GlobalExceptionHandler` | one `ErrorResponse` shape |
| API docs | `config/OpenApiConfig` | Swagger UI + bearer scheme |
| Startup seeding | `config/DataInitializer` | dev-only admin user |
| Schema | `resources/db/migration/*` | Flyway, versioned |

---

## Request lifecycle (an authenticated call)

1. `JwtAuthenticationFilter` runs once per request: reads `Authorization: Bearer <token>`,
   validates it via `JwtService`, loads the user via `AppUserDetailsService`, and sets the
   `SecurityContext`.
2. `SecurityConfig`'s filter chain checks authorization: public paths pass; everything else
   requires an authentication in the context, else `RestAuthenticationEntryPoint` returns a JSON 401.
3. The controller method runs; `@AuthenticationPrincipal AppUserDetails` exposes the current user.
4. The service does the work inside a transaction and returns a DTO.
5. Any thrown `ApiException` (or validation/auth exception) is caught by `GlobalExceptionHandler`
   and rendered as `ErrorResponse`.

---

## Configuration & profiles

- `application.yml` — defaults: H2 in-memory, Flyway on, `/v1` context path, Swagger, health.
- `application-prod.yml` — PostgreSQL; all values from env vars (`DB_URL`, `DB_USERNAME`,
  `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`). Activate with
  `--spring.profiles.active=prod`.
- Secrets are **never** committed — only env-var placeholders with dev-only fallbacks.

---

## Deliberately NOT included (keep it lean)

- No Lombok (plain Java; avoids annotation-processor coupling).
- No mapper framework (MapStruct/ModelMapper) — static `from()` factories are enough at this size.
- No CQRS, no hexagonal ports/adapters — a layered service is the right weight for this app.
- No token blacklist — add only if a real revocation requirement appears.
Introduce any of these only when a concrete need justifies the extra complexity.
