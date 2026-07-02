# SPK Mainan — Backend

REST API for the SPK Mainan (toy decision-support) app. Built with **Spring Boot 3.4 / Java 21**,
using JWT auth, Spring Data JPA, and Flyway migrations. Package-by-feature layout.

## Requirements

- JDK 21+ (a `java` on your `PATH`)
- No Maven install needed — use the bundled wrapper (`./mvnw`)

## Run (dev — in-memory H2, zero setup)

```bash
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080/v1` (context path `/v1`, matching the mobile app's
`ApiConfig.baseUrl`). A demo user is seeded automatically:

| username | password    | role  |
|----------|-------------|-------|
| `admin`  | `password123` | admin |

### Handy URLs (dev)

- Swagger UI:  `http://localhost:8080/v1/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v1/v3/api-docs`
- Health:      `http://localhost:8080/v1/actuator/health`
- H2 console:  `http://localhost:8080/v1/h2-console` (JDBC URL `jdbc:h2:mem:spkmainan`, user `sa`)

## Test

```bash
./mvnw test
```

## Scaffold a new feature (fast path)

Generate a full CRUD feature (entity, repository, paged service, controller, DTOs, migration,
tests) instead of hand-writing boilerplate, then edit the fields:

```bash
scripts/new-feature.sh Criterion criteria   # → /v1/criteria, table `criteria`
./mvnw test
```

See [docs/02_DEVELOPMENT_GUIDE.md](docs/02_DEVELOPMENT_GUIDE.md) (Recipe 0).

## API (implemented)

| Method | Path            | Auth   | Body / Notes                                   |
|--------|-----------------|--------|------------------------------------------------|
| POST   | `/auth/login`   | public | `{ "username", "password" }` → `{ user, token }` |
| POST   | `/auth/logout`  | public | 204 (stateless; client discards the token)     |
| GET    | `/auth/profile` | bearer | current user                                   |

Send the token as `Authorization: Bearer <token>`.

### Quick check

```bash
# login
curl -s -X POST http://localhost:8080/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password123"}'

# profile (paste the token)
curl -s http://localhost:8080/v1/auth/profile -H "Authorization: Bearer <token>"
```

## Production (PostgreSQL)

Activate the `prod` profile and provide config via env vars:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/spk_mainan
export DB_USERNAME=spk_mainan
export DB_PASSWORD=change-me
export JWT_SECRET=a-long-random-256-bit-secret
export CORS_ALLOWED_ORIGINS=https://yourapp.example.com

./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
# or: java -jar target/*.jar --spring.profiles.active=prod
```

The demo-user seeder is disabled under `prod`.

## Structure (package-by-feature)

```
com.spkmainan
├── auth/          # login/logout/profile — controller, service, DTOs
├── user/          # User entity, repository, Role, UserResponse DTO
├── security/      # JWT service + filter, UserDetails adapter, 401 entry point
├── common/        # cross-cutting: error handling (ErrorResponse, handlers)
└── config/        # SecurityConfig, OpenApiConfig, DataInitializer (dev seed)
```

## Next steps (SPK domain)

The auth slice is a **template**. Add the toy decision-support features the same way —
one package per feature (e.g. `criteria/`, `alternative/`, `scoring/`) with its own
controller/service/repository/DTOs, and a Flyway migration (`V2__...sql`, `V3__...sql`) per
schema change. Pick the SPK method (SAW / WP / TOPSIS / AHP) first — see the mobile app's
`docs/05_ROADMAP.md`.
