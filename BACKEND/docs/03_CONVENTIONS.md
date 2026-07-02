# 03 — Conventions

See also [CLAUDE.md](../CLAUDE.md) — the source of truth for the strict rules. This doc is the
quick reference.

---

## Naming

| Thing | Rule | Example |
|---|---|---|
| Package | lowercase, singular, feature-named | `auth`, `user`, `criteria` |
| Class | `PascalCase` | `AuthService` |
| Controller | suffix `Controller` | `AuthController` |
| Service | suffix `Service` | `AuthService` |
| Repository | suffix `Repository` (interface) | `UserRepository` |
| Entity | plain noun, no suffix | `User`, `Criterion` |
| Request DTO | suffix `Request` (record) | `LoginRequest` |
| Response DTO | suffix `Response` (record) | `UserResponse` |
| Enum | `PascalCase`, values `UPPER_SNAKE` | `Role.ADMIN` |
| Methods / fields | `camelCase` | `findByUsername` |
| Constants | `UPPER_SNAKE_CASE` | `PUBLIC_PATHS` |
| Migration file | `V<n>__snake_desc.sql` | `V2__init_criteria.sql` |

---

## Package / file layout

- One package per feature (`com.spkmainan.<feature>`); DTOs in its `dto/` sub-package.
- One top-level class per file.
- Cross-cutting: `security/`, `common/` (e.g. `common/exception/`). Wiring: `config/`.
- REST paths: lowercase, **plural** nouns, no trailing slash (`/criteria`, `/alternatives`).

---

## Layer responsibilities (do / don't)

| Layer | Do | Don't |
|---|---|---|
| Controller | map routes, `@Valid`, return DTOs | business logic, touch repos/entities |
| Service | logic, `@Transactional`, entity→DTO, throw `ApiException` | build error `ResponseEntity`s |
| Repository | query methods | logic |
| Entity | persistence model | leave the service layer |

---

## DTOs & JSON

- DTOs are `record`s. Map with a static `from(entity)` factory.
- Match the mobile JSON contract; use `@JsonProperty("snake_case")` where needed.
- Never expose `password` or internal-only fields.
- Validate requests with `jakarta.validation` (`@NotBlank`, `@NotNull`, `@Email`, `@DecimalMin`…)
  + `@Valid` on the controller parameter.

---

## Dependency injection

- **Constructor injection only.** `final` fields, one constructor (no `@Autowired` needed).
- No field or setter injection. No `@Autowired` on fields.

---

## Errors

- Throw `ApiException` subclasses (`ResourceNotFoundException`, …) — never return ad-hoc error bodies.
- All errors render as `ErrorResponse` via `GlobalExceptionHandler`. Add a new handler there only
  for a genuinely new exception category.

---

## Transactions

- `@Transactional(readOnly = true)` on read methods; `@Transactional` on writes.
- Put `@Transactional` on **service** methods, not controllers or repositories.

---

## Database

- Schema owned by Flyway; `ddl-auto: validate`. Never `create`/`update`.
- Portable ANSI SQL (H2 v2 + PostgreSQL). Entity columns must match the migration.
- Timestamps via Hibernate `@CreationTimestamp` / `@UpdateTimestamp` on `Instant` fields.

---

## Configuration & secrets

- No secrets in the repo. Reference env vars in `application*.yml`:
  `${JWT_SECRET:dev-fallback}`. Production sets real values.
- Dev defaults must let the app run with zero external setup (H2, seeded user).

---

## Code style

- Comments/docs in **English**; explain *why*.
- Early returns over nested `if`. Small methods. Logic out of controllers.
- Logging via SLF4J (`private static final Logger log = LoggerFactory.getLogger(X.class)`).
  No `System.out`/`printStackTrace`.
- `final` for fields and locals that don't change.

---

## Testing

- Framework: JUnit 5 + Spring Boot Test + `MockMvc` (+ `spring-security-test`).
- Tests run against H2 with Flyway applied (`src/test/resources/application.yml`).
- Each feature: at least one happy-path and one failure-path test (mirror `AuthControllerTest`).
- Authenticate protected endpoints with `@WithMockUser` or a real login-then-bearer flow.

---

## Definition of Done

`./mvnw test` passes (clean compile + green tests). Client-facing changes are reflected in
[04_API_REFERENCE.md](04_API_REFERENCE.md); status changes in [05_ROADMAP.md](05_ROADMAP.md).
