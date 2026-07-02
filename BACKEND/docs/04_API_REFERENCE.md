# 04 — API Reference (Contract with the Mobile App)

The authoritative, browsable spec is **Swagger UI** at
`http://localhost:8080/v1/swagger-ui.html` (OpenAPI JSON at `/v1/v3/api-docs`). This file
documents the stable contract the Flutter app depends on — **keep it in sync** with
`../../MOBILE/lib/config/api_config.dart` and `../../MOBILE/lib/data/models/user.dart`.

---

## Conventions

- **Base URL:** `http://<host>:8080/v1` (context path `/v1`, matching the mobile `ApiConfig.baseUrl`).
- **Auth:** JWT bearer — `Authorization: Bearer <token>`. Stateless.
- **Content type:** `application/json` for requests and responses.
- **JSON casing:** snake_case for multi-word fields the client reads (e.g. `avatar_url`).
- **Errors:** every failure returns the `ErrorResponse` shape (below) with the matching HTTP status.

---

## Error shape (all endpoints)

```json
{
  "timestamp": "2026-07-01T13:59:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/v1/auth/login",
  "fieldErrors": { "username": "Username is required" }
}
```
`fieldErrors` is present only for validation failures.

| Status | When |
|---|---|
| 400 | request body fails validation |
| 401 | missing/invalid token, or bad credentials on login |
| 404 | resource not found (`ResourceNotFoundException`) |
| 500 | unexpected server error |

---

## Endpoints (implemented)

### POST `/auth/login` — public
Request:
```json
{ "username": "admin", "password": "password123" }
```
Response `200`:
```json
{
  "user": { "id": "1", "name": "Administrator", "email": "admin@spkmainan.test",
            "avatar_url": null, "role": "admin" },
  "token": "eyJhbGciOi..."
}
```
`401` on bad credentials, `400` on blank fields.

### POST `/auth/logout` — public
No body. Response `204`. Stateless: the client discards the token; the server does nothing.

### GET `/auth/profile` — bearer
Response `200` — the current user (same object as `user` above). `401` without a valid token.

---

## The `user` object

Matches `User.fromJson` in the mobile app. Any change here must be mirrored there.

| Field | Type | Notes |
|---|---|---|
| `id` | string | numeric id serialized as string |
| `name` | string | display name |
| `email` | string | unique |
| `avatar_url` | string \| null | snake_case |
| `role` | string | lowercased (`admin`, `user`) |

---

## Dev credentials

Seeded automatically in non-`prod` profiles by `DataInitializer`:

| username | password | role |
|---|---|---|
| `admin` | `password123` | admin |

---

## Planned (not yet implemented)

SPK domain endpoints — decide the shape in [05_ROADMAP.md](05_ROADMAP.md) before building. Likely:
`/criteria`, `/alternatives`, and a scoring/ranking endpoint. Document each here as it ships.
