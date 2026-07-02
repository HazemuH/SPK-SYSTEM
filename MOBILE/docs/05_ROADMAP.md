# 05 — Roadmap & Feature Tracker

> A living document. **Update it whenever you start/finish a feature** so development direction
> survives across sessions. Reflect major status changes in
> [00_PROJECT_CONTEXT.md](00_PROJECT_CONTEXT.md) too.

---

## 🎯 Product goal (FILL THIS IN FIRST)

> ⚠️ Not yet defined in code. Decide this before building core features.

- **Problem it solves:** _(e.g. help users choose the best toy via an SPK method)_
- **Target users:** _(…)_
- **SPK method:** _(e.g. SAW / WP / TOPSIS / AHP — pick one or more)_
- **Core domain entities:**
  - Criteria — _(…)_
  - Alternatives (toys) — _(…)_
  - Weights & scores — _(…)_
  - Ranking result — _(…)_

---

## 🧱 Foundation (done)

- [x] Lightweight layered MVVM + Repository structure (ui / domain-optional / data)
- [x] Design system (colors, spacing, typography, shared widget library)
- [x] `ApiClient` (dio) + interceptors (auth, error mapping to `AppException`)
- [x] `go_router` + auth-aware redirect
- [x] Auth feature slice (template) — **note: login is still mocked**
- [x] `flutter analyze` clean, `flutter test` green

---

## 🚧 Backlog (prioritize & fill in)

Move items to "Done" as they ship.

| Priority | Feature | Status | Notes |
|---|---|---|---|
| 🔴 | Define the SPK domain | ⬜ Todo | fill in "Product goal" above |
| 🔴 | Connect the real backend | ⬜ Todo | set `baseUrl`, remove the mock auth block |
| 🔴 | First feature: _____ | ⬜ Todo | follow Recipe A in [02_DEVELOPMENT_GUIDE.md](02_DEVELOPMENT_GUIDE.md) |
| 🟡 | Criteria management | ⬜ Todo | |
| 🟡 | Alternatives (toys) management | ⬜ Todo | |
| 🟡 | SPK scoring & ranking | ⬜ Todo | likely a `domain/use_cases/` class |
| 🟡 | View-model / repository tests | ⬜ Todo | |
| 🟢 | Offline detection (`NetworkInfo` / `connectivity_plus`) | ⬜ Todo | not wired yet |

Status legend: ⬜ Todo · 🔄 In Progress · ✅ Done · ⏸️ Blocked

---

## ✅ Done

_(empty — move items here as they ship, with a date)_

---

## 📝 New-feature entry template

Copy this block when starting a feature:

```markdown
### Feature: <name>
- **Start date:** YYYY-MM-DD
- **Goal:** what the user achieves
- **Layers touched:** data ☐  domain ☐  ui ☐  routing ☐
- **API endpoint:** GET/POST ...
- **New screen(s):** ...
- **New view model(s):** ...
- **Definition of done:** analyze clean ☐ · tests green ☐ · 3 UI states handled ☐
- **Notes / decisions:**
```

---

## 🧭 Decision Log

Record important architectural/technical decisions so they aren't re-litigated.

| Date | Decision | Reason |
|---|---|---|
| 2026-06-23 | Adopt lightweight layered MVVM + Repository (official Flutter guidance) | suitable for a small app; less boilerplate than strict Clean Architecture |
| 2026-06-23 | Modern Riverpod `Notifier` API, manual providers (no codegen) | simpler toolchain; no build_runner step |
| 2026-06-23 | Single model per concept + single `AppException` hierarchy | avoid entity/DTO and exception/failure duplication |
| | | |
