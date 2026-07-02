# 00 — Project Context (Current Status)

> **Purpose:** give anyone (human or AI assistant) enough context to continue development
> without losing direction, even when starting from a blank session.
> **Update this file whenever a major status changes.**

Last updated: **2026-06-23**

---

## 1. What is this project?

`spk_mainan` — a Flutter mobile app. "SPK" suggests a **Decision Support System** (Sistem
Pendukung Keputusan) in the **toys** ("mainan") domain. The concrete business domain (criteria,
alternatives, the SPK scoring method) is **not yet defined in code** — it must be decided in
[05_ROADMAP.md](05_ROADMAP.md) before building the core features.

---

## 2. What already exists ✅

A complete **lightweight layered MVVM + Repository** foundation, with one example feature slice:
**Auth (login/logout)**.

### Architecture
Follows the official Flutter app-architecture guidance. Layers: `ui` (views + view models) →
`domain` (optional use cases) → `data` (repositories + services + models). See
[01_ARCHITECTURE.md](01_ARCHITECTURE.md).

### `config/` and `routing/`
- `config/app_config.dart`, `config/api_config.dart` — static configuration.
- `routing/router.dart`, `routing/routes.dart` — `go_router` with an auth-aware redirect.

### `core/` (cross-cutting)
- `exceptions/app_exception.dart` — a single sealed error hierarchy.
- `network/api_client.dart` — configured `Dio` with an auth interceptor (auto bearer token) and
  error mapping (401/422/5xx/timeout → `AppException`). Plus `network_info.dart`.
- `storage/storage_keys.dart`, `utils/` (`validators`, `formatter`, `extensions`).

### Example feature: Auth (use it as a template)
```
data/models/user.dart                       # single model (no entity/DTO split)
data/services/auth_api_service.dart          # API calls + provider
data/repositories/auth_repository.dart       # orchestration + provider
ui/auth/view_model/auth_view_model.dart      # Notifier-based view model + provider
ui/auth/widgets/login_screen.dart            # the view
```

### Screens
- `SplashScreen` — restores the session, then redirects.
- `LoginScreen` — full form, loading state, errors via snackbar.
- `DashboardScreen` — "Coming Soon" placeholder + logout.

### Design system & shared widgets (`ui/core/`)
- `themes/` — `AppColors`, `AppSpacing`, `AppTypography`, `AppTheme` (light + dark).
- `widgets/` — `AppButton`, `AppTextField`, `AppCard`, `AppSnackbar`, `AppLoading`,
  `AppErrorView`, `AppEmptyState`, `ScreenWrapper`. See [03_DESIGN_SYSTEM.md](03_DESIGN_SYSTEM.md).

### Quality gates
- `flutter analyze` → clean. `flutter test` → green (a widget test for `AppButton`).

---

## 3. What is still mocked ⚠️

Things that are **not production-ready** — do not assume they work end-to-end:

### Auth is mocked
In [`auth_view_model.dart`](../lib/ui/auth/view_model/auth_view_model.dart), `login()` has a
clearly marked `MOCK AUTH` block: **any non-empty credentials succeed** with a fake user/token.
The real-API path exists right below it but is unreachable while the mock block is present.

**To use the real API:** remove the `MOCK AUTH` block in `AuthViewModel.login()`, then set
`ApiConfig.baseUrl` to the real backend URL.

### Placeholder base URL
`ApiConfig.baseUrl = 'https://api.example.com/v1'` — there is **no real backend** yet.

### `NetworkInfo` not wired
[`network_info.dart`](../lib/core/network/network_info.dart) exists but is not injected anywhere.
The `ApiClient` does map timeouts/connection errors to `NetworkException`. There is no
`connectivity_plus` dependency.

### Minimal tests
Only `test/ui/core/widgets/app_button_test.dart` exists. No repository/view-model tests yet.

---

## 4. Is it "ready to develop features"?

**Yes — the foundation is ready.** The architecture, design system, networking and routing are
in place, with Auth as an end-to-end example to copy. Before/while building the first business
feature:

| # | Action | Priority |
|---|---|---|
| 1 | Define the SPK business domain in [05_ROADMAP.md](05_ROADMAP.md) (criteria, alternatives, method) | 🔴 High |
| 2 | Connect a real backend (set `baseUrl`, remove the mock auth block) | 🔴 High |
| 3 | Build the first feature following [02_DEVELOPMENT_GUIDE.md](02_DEVELOPMENT_GUIDE.md) | 🔴 High |
| 4 | Add view-model/repository tests for new features | 🟡 Medium |
| 5 | Wire `NetworkInfo` (or add `connectivity_plus`) if you need offline detection | 🟢 Low |

---

## 5. Recommended next steps

1. Fill in **[05_ROADMAP.md](05_ROADMAP.md)** with the product goal and concrete features.
2. Pick the first feature (e.g. "Toy List" / "Criteria Input").
3. Follow the **"Add a New Feature"** recipe in [02_DEVELOPMENT_GUIDE.md](02_DEVELOPMENT_GUIDE.md).
4. Update the "Status" sections here when the feature is done.

---

## 6. Technical notes (gotchas)

- View models use the modern Riverpod `Notifier` API. There is **no codegen/build_runner** — all
  providers are written manually and colocated with what they provide.
- The data layer throws `AppException` (one sealed hierarchy); view models catch with
  `on AppException` and `switch` on subtypes to produce Indonesian messages.
- The splash screen runs a fixed delay timer; full-app widget tests must drive that timer
  explicitly. That is why the smoke test targets a leaf widget instead — see
  [04_CONVENTIONS.md](04_CONVENTIONS.md#testing).
