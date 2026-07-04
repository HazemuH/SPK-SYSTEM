# 00 — Project Context (Current Status)

> **Purpose:** give anyone (human or AI assistant) enough context to continue development
> without losing direction, even when starting from a blank session.
> **Update this file whenever a major status changes.**

Last updated: **2026-07-05**

---

## 1. What is this project?

`spk_mainan` — a Flutter mobile app for a **Decision Support System** (Sistem Pendukung Keputusan)
in the **toys** ("mainan") domain.

**Product model (decided):**
- **Mobile (this app)** — **read-only, NO login**. Used by the end user / shop staff to *view*
  results (e.g. the recommended toys). Calls only PUBLIC backend endpoints.
- **Web** (separate `../FRONTEND`) — has login; used to **manage** criteria/toys/formulas.
- **Backend** (`../BACKEND`) serves both.

The concrete SPK scoring method (SAW / WP / TOPSIS / "pure" weighting) is **not yet decided** —
this is safe to defer: it only affects the ranking *calculation* on the backend, not the shared
data model or these read-only screens.

---

## 2. What already exists ✅

A **lightweight layered MVVM + Repository** foundation, with one example feature slice:
**Home — toy recommendations (read-only)**.

### Architecture
Layers: `ui` (views + view models) → `domain` (optional) → `data` (repositories + services +
models). See [01_ARCHITECTURE.md](01_ARCHITECTURE.md).

### `config/` and `routing/`
- `config/app_config.dart`, `config/api_config.dart` — static config (public endpoints only).
- `routing/router.dart`, `routing/routes.dart` — `go_router`, **no auth redirect**; opens straight
  to `home`.

### `core/` (cross-cutting)
- `exceptions/app_exception.dart` — a single sealed error hierarchy.
- `network/api_client.dart` — configured `Dio` with error mapping (401/422/5xx/timeout →
  `AppException`). It still carries a (harmless, unused) bearer interceptor; there is no login.
- `storage/`, `utils/` (`validators`, `formatter`, `extensions`).

### Example feature: Home (use it as the template)
```
data/models/toy.dart                       # single model (fromJson/toJson)
data/services/toy_api_service.dart          # public API call + provider (MOCK for now)
data/repositories/toy_repository.dart       # orchestration + provider
ui/home/view_model/home_view_model.dart     # AsyncNotifier<List<Toy>> + provider
ui/home/widgets/home_screen.dart            # the read-only view (loading/error/empty/data)
```

### Screens
- `HomeScreen` — the entry screen. Read-only ranked list of recommended toys, pull-to-refresh.

### Design system & shared widgets (`ui/core/`)
- `themes/` — `AppColors`, `AppSpacing`, `AppTypography`, `AppTheme` (light + dark).
- `widgets/` — `AppButton`, `AppTextField`, `AppCard`, `AppSnackbar`, `AppLoading`,
  `AppErrorView`, `AppEmptyState`, `ScreenWrapper`. See [03_DESIGN_SYSTEM.md](03_DESIGN_SYSTEM.md).

### Quality gates
- `flutter analyze` → clean. `flutter test` → green (`AppButton` + `HomeScreen`).

---

## 3. What is still mocked ⚠️

### Toy data is mocked
In [`toy_api_service.dart`](../lib/data/services/toy_api_service.dart), `fetchRecommendations()`
returns a hardcoded list. The real (public) API call exists right below it but is unreachable
while the mock block is present.

**To use the real API:** remove the mock block in `ToyApiService.fetchRecommendations()`, then set
`ApiConfig.baseUrl` (Android emulator: `http://10.0.2.2:8080/v1`) once the backend exposes
`/toys/recommendations`.

### Placeholder base URL
`ApiConfig.baseUrl = 'https://api.example.com/v1'` — not pointing at a real backend yet.

### `NetworkInfo` not wired
`network_info.dart` exists but is not injected anywhere. There is no `connectivity_plus` dependency.

---

## 4. Is it "ready to develop features"?

**Yes — the foundation is ready** and login-less, with Home as an end-to-end read-only example to
copy.

| # | Action | Priority |
|---|---|---|
| 1 | Backend: public read endpoint(s) for the display data (`/toys`, `/toys/recommendations`) | 🔴 High |
| 2 | Swap the mock in `toy_api_service.dart` for the real call + set `baseUrl` | 🔴 High |
| 3 | Add more read-only screens (e.g. toy detail) following [02_DEVELOPMENT_GUIDE.md](02_DEVELOPMENT_GUIDE.md) | 🟡 Medium |
| 4 | Decide the SPK method (affects backend calculation only) | 🟢 Low (deferrable) |

---

## 5. Recommended next steps

1. Build the backend public display endpoints.
2. Point `ApiConfig.baseUrl` at them and remove the mock block.
3. Add further read-only screens; keep handling loading/error/empty in every one.
4. Update the "Status" sections here when things change.

---

## 6. Technical notes (gotchas)

- View models use the modern Riverpod `Notifier`/`AsyncNotifier` API. There is **no
  codegen/build_runner** — providers are written manually and colocated with what they provide.
- The data layer throws `AppException` (one sealed hierarchy); view models catch with
  `on AppException` and `switch` on subtypes to produce Indonesian messages.
- The app is login-less: `api_client.dart` never receives a token, so its bearer interceptor is a
  no-op. Its value now is the error mapping.
