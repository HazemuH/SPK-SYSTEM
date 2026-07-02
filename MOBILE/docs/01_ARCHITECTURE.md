# 01 — Architecture

A **lightweight layered MVVM + Repository** architecture, following the official Flutter
app-architecture guidance (https://docs.flutter.dev/app-architecture). Optimized for a small
app: minimal boilerplate, still testable. See also [CLAUDE.md](../CLAUDE.md).

---

## Data flow

```
View (Widget)  →  ViewModel (Notifier)  →  Repository  →  Service (API)  →  ApiClient (dio)  →  API
                          └────────── UseCase (OPTIONAL) ──────────┘
```

- Widgets never call dio/services directly — always go through a repository.
- Business logic never lives in widgets.
- Dependencies point inward (`ui → domain → data`); the data layer never imports `ui`.
- A **use case** is optional — add one only when logic is shared by multiple view models or is
  complex enough to deserve isolated tests. Do not create one per action by default.

---

## Layers & responsibilities

### `ui/` — Views + View Models (feature-first)
| Folder | Holds | Example |
|---|---|---|
| `ui/<feature>/widgets/` | screens + feature-only widgets | `auth/widgets/login_screen.dart` |
| `ui/<feature>/view_model/` | `Notifier`/`AsyncNotifier` + its provider | `auth/view_model/auth_view_model.dart` |
| `ui/core/themes/` | design tokens | `colors.dart`, `theme.dart` |
| `ui/core/widgets/` | shared widgets | `app_button.dart` |

A **view model** owns screen state (immutable state class) and calls repositories. It exposes a
provider (`NotifierProvider`).

### `domain/` — OPTIONAL
Add `use_cases/` (and `models/`) only when needed. A use case is a single class with a `call()`
method wrapping one repository interaction that is shared or complex.

### `data/` — Repositories + Services + Models
| Folder | Holds | Example |
|---|---|---|
| `data/models/` | data classes with `fromJson`/`toJson` | `user.dart` |
| `data/services/` | API access (the only place that knows dio) | `auth_api_service.dart` |
| `data/repositories/` | orchestration of services + local storage | `auth_repository.dart` |

A single **concrete repository** is the default. Add an abstract interface only when you need
multiple implementations or to fake the whole repository in tests.

### `core/` — Cross-cutting (no business logic)
`exceptions/`, `network/`, `storage/`, `utils/`.

### `config/` and `routing/`
Static configuration (`app_config`, `api_config`) and the `go_router` setup.

---

## Error handling: one hierarchy

There is a **single** sealed `AppException` hierarchy in
[`core/exceptions/app_exception.dart`](../lib/core/exceptions/app_exception.dart):
`ServerException`, `UnauthorizedException`, `ValidationException`, `NetworkException`,
`CacheException`.

| Where | What happens |
|---|---|
| `ApiClient` interceptor | maps HTTP errors → `AppException` (401→Unauthorized + clears token, 422→Validation, 5xx→Server, timeout→Network) |
| Service | unwraps the `AppException` from the `DioException`, or wraps anything else as `ServerException` |
| Repository | lets `AppException` propagate (or catches to return a fallback, e.g. `currentUser` returns `null`) |
| ViewModel | `try { … } on AppException catch (e)` and `switch (e)` to build an Indonesian message |

Because `AppException` is `sealed`, the `switch` in a view model is exhaustive — the compiler
flags a missing case.

---

## State management (Riverpod)

- View models extend `Notifier<State>` / `AsyncNotifier<T>` (modern API — not the legacy
  `StateNotifier`). Read dependencies via `ref.read(...)` inside the notifier.
- Expose with `NotifierProvider` / `AsyncNotifierProvider`.
- Read-only async data can use `FutureProvider` / `StreamProvider`.
- No `setState` for business state. `setState` is fine for purely local widget UI.
- Providers are colocated with the thing they provide (e.g. `authRepositoryProvider` lives in
  `auth_repository.dart`). Manual providers only — no build_runner/codegen.

Example dependency chain for auth:
`secureStorageProvider` + `apiClientProvider` → `authApiServiceProvider` →
`authRepositoryProvider` → `authViewModelProvider`.

---

## Navigation

- `go_router` only. No `Navigator.push` directly.
- Reference paths from `Routes` (`routing/routes.dart`); don't hardcode strings.
- The redirect in `routerProvider` watches `authViewModelProvider` and waits for
  `AuthState.isInitialized` before routing.
- Pass data via `extra` / path params, not via screen constructors.
