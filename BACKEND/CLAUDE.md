# CLAUDE.md — Agent Instructions (Backend)

## Project Overview
REST API backend for **SPK Mainan**, a toy (mainan) decision-support app. Built with
**Spring Boot 3.4 / Java 21**, using a **layered, package-by-feature** architecture. It serves
the Flutter app in `../MOBILE` — the API contract must stay in sync with it (see
[docs/04_API_REFERENCE.md](docs/04_API_REFERENCE.md)). **Read this entire file before writing any code.**

All comments and documentation are written in **English**. Client-facing error `message` strings
may be plain English; the mobile app localizes to Indonesian on its side.

## Tech Stack
Java 21, Spring Boot 3.4 (Web, Security, Data JPA, Validation, Actuator), JWT (jjwt 0.12),
Flyway, H2 (dev/test) / PostgreSQL (prod), springdoc-openapi (Swagger). Build: Maven via the
wrapper (`./mvnw`). **No Lombok** — write plain Java (records for DTOs, explicit accessors on entities).

## Fastest path — scaffold, then fill in (vibe coding)
Don't hand-write boilerplate. Generate a full CRUD feature and edit the fields:

```bash
scripts/new-feature.sh Product          # → package `product`, table/path `products`
scripts/new-feature.sh ToyCriterion criteria
```

This stamps a compiling entity + repository + service (paged CRUD) + controller + DTOs +
Flyway migration + tests, all following the conventions below. Then: edit the placeholder
`name` field in the entity, migration, and DTOs to your real columns, and run `./mvnw test`.

Reusable building blocks to lean on (don't reinvent):
- `common/domain/BaseEntity` — extend it; gives `id` + `created_at`/`updated_at`. Never redeclare these.
- `common/dto/PageResponse<T>` — return this from list endpoints (`PageResponse.from(page)`).
- `common/exception/` — throw `ResourceNotFoundException` (404), `BadRequestException` (400),
  `ConflictException` (409); `GlobalExceptionHandler` renders them.
- `@EnableMethodSecurity` is on, so `@PreAuthorize("hasRole('ADMIN')")` works on any method.

## Architecture — Layered, Package-by-Feature
One package per feature. Within a feature, dependencies point one way:

```
Controller  →  Service  →  Repository  →  Entity (JPA)
   (DTO in)     (logic,      (Spring Data)
   (DTO out)   @Transactional)
```

- **Controller** — HTTP only: maps routes, validates input, returns DTOs. No business logic, no
  entity leakage.
- **Service** — business logic + transaction boundaries (`@Transactional`). Throws `ApiException`
  subclasses on failure.
- **Repository** — `JpaRepository` interfaces. Query methods only; no logic.
- **Entity** — JPA `@Entity`, plain POJO. Never returned from a controller.

### Rules
- NEVER return or accept a JPA `@Entity` in a controller — always a DTO (`record`) at the boundary.
- NEVER put business logic in a controller or repository — it belongs in a service.
- ALWAYS use **constructor injection** (no `@Autowired` on fields, no field/setter injection).
- ALWAYS throw an `ApiException` subclass (e.g. `ResourceNotFoundException`) for expected errors;
  let `GlobalExceptionHandler` render the response. Do NOT build `ResponseEntity` error bodies by hand.
- ALWAYS validate request DTOs with `jakarta.validation` annotations + `@Valid` on the controller param.
- The database schema is owned by **Flyway** (`ddl-auto: validate`). NEVER switch to `create`/`update`;
  every schema change is a new `V<n>__*.sql` migration.
- Keep secrets out of the repo — read them from env vars via `application*.yml` placeholders.

## Package Structure
```
com.spkmainan
├── SpkMainanApplication.java
├── auth/               # feature: login/logout/profile
│   ├── AuthController.java
│   ├── AuthService.java
│   └── dto/            # LoginRequest, LoginResponse (records)
├── user/               # feature: users
│   ├── User.java           # @Entity
│   ├── UserRepository.java  # JpaRepository
│   ├── Role.java            # enum
│   └── dto/                 # UserResponse (record)
├── security/           # JwtService, JwtAuthenticationFilter,
│                       #   AppUserDetails(+Service), RestAuthenticationEntryPoint
├── common/
│   ├── domain/         # BaseEntity (@MappedSuperclass: id + timestamps)
│   ├── dto/            # PageResponse<T> (pagination envelope)
│   └── exception/      # ApiException + subclasses, ErrorResponse, GlobalExceptionHandler
└── config/             # SecurityConfig, OpenApiConfig, DataInitializer

scripts/new-feature.sh  # generator: stamps a full CRUD feature package
```

A **new feature** = a new package `com.spkmainan.<feature>` containing its controller, service,
repository, entity/entities, and a `dto/` sub-package. Cross-cutting code goes in `common/` or
`security/`; wiring/config goes in `config/`.

## DTO Rules
- Request and response DTOs are Java **`record`s**, placed in the feature's `dto/` package.
- Map entity → DTO with a static factory on the DTO (e.g. `UserResponse.from(user)`). Do not add
  a mapping framework for this size of project.
- JSON field names must match the mobile client. Use `@JsonProperty("snake_case")` when the Dart
  side expects snake_case (e.g. `avatar_url`).
- Never expose `password` or other sensitive fields in a response DTO.

## Error Handling
- One error body shape for the whole API: `ErrorResponse` (see `common/exception/`).
- Throw `ApiException` (or a subclass) with an `HttpStatus`; `GlobalExceptionHandler`
  (`@RestControllerAdvice`) converts it. Validation failures (`MethodArgumentNotValidException`)
  and auth failures are already handled centrally — do not duplicate that per-controller.

## Security
- Stateless JWT (`Authorization: Bearer <token>`); no server session. Passwords hashed with BCrypt.
- New endpoints are **authenticated by default**. To make one public, add its path to
  `PUBLIC_PATHS` in `SecurityConfig` (paths are relative to the `/v1` context path).
- Get the current user with `@AuthenticationPrincipal AppUserDetails principal` in the controller.
- Restrict by role with `hasRole("ADMIN")` / `@PreAuthorize` (authorities are `ROLE_<NAME>`).

## Database & Migrations
- Every schema change is a new immutable file: `src/main/resources/db/migration/V<n>__<desc>.sql`.
  Never edit an already-applied migration — add a new one.
- Write portable SQL that runs on both H2 (v2, PostgreSQL mode) and PostgreSQL. Prefer ANSI:
  `BIGINT GENERATED BY DEFAULT AS IDENTITY`, `TIMESTAMP`, `VARCHAR(n)`.
- Entity column names/types must match the migration (JPA runs `validate` on startup).

## Naming Conventions
- Packages: lowercase, singular, feature-named (`auth`, `user`, `criteria`).
- Classes: `PascalCase`. Controllers end `Controller`, services `Service`, repositories `Repository`,
  entities are plain nouns (`User`), request DTOs end `Request`, response DTOs end `Response`.
- Methods/fields: `camelCase`. Constants: `UPPER_SNAKE_CASE`.
- REST paths: lowercase, plural nouns (`/criteria`, `/alternatives`); no trailing slash.

## Code Style
- Comments in English; explain *why*, not *what*. Prefer early returns over nested `if`.
- Constructor injection, `final` fields. One top-level class per file.
- No field injection, no `System.out.println` — use SLF4J (`private static final Logger log = ...`).
- Keep methods small; push logic out of controllers into services.

## Definition of Done
`./mvnw test` passes (compile clean + all tests green) before any change is considered complete.
If you add a feature, add at least one test for its happy path and one for its main failure path,
mirroring `AuthControllerTest`.

## Key References
- Architecture deep-dive → [docs/01_ARCHITECTURE.md](docs/01_ARCHITECTURE.md)
- Step-by-step recipes → [docs/02_DEVELOPMENT_GUIDE.md](docs/02_DEVELOPMENT_GUIDE.md)
- Conventions → [docs/03_CONVENTIONS.md](docs/03_CONVENTIONS.md)
- API contract with mobile → [docs/04_API_REFERENCE.md](docs/04_API_REFERENCE.md)
- Status & roadmap → [docs/00_PROJECT_CONTEXT.md](docs/00_PROJECT_CONTEXT.md), [docs/05_ROADMAP.md](docs/05_ROADMAP.md)
