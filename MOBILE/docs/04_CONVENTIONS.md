# 04 — Conventions

See also [CLAUDE.md](../CLAUDE.md) (the source of truth for the strict rules).

---

## Naming

| Thing | Rule | Example |
|---|---|---|
| Files & folders | `snake_case` | `product_list_view_model.dart` |
| Classes | `PascalCase` | `ProductListScreen` |
| Variables / methods | `camelCase` | `getProducts()` |
| Primitive constants | `camelCase` value, `SCREAMING_SNAKE` only for true consts | `splashDuration` |
| View model | suffix `ViewModel` | `ProductListViewModel` |
| View model provider | suffix `ViewModelProvider` | `productListViewModelProvider` |
| Repository | suffix `Repository` (concrete) | `ProductRepository` |
| Service | suffix `Service` / `ApiService` | `ProductApiService` |
| Model | plain noun, no suffix | `Product`, `User` |
| Screen | suffix `Screen` | `ProductListScreen` |
| Use case | suffix `UseCase` | `RankProductsUseCase` |

### File suffixes per layer
`*.dart` model (plain) · `*_api_service.dart` · `*_repository.dart` · `*_view_model.dart` ·
`*_screen.dart` · `*_use_case.dart` (optional).

---

## Folder conventions

- Feature-first under `ui/<feature>/` with `view_model/` and `widgets/`.
- Shared widgets in `ui/core/widgets/`; design tokens in `ui/core/themes/`.
- Cross-cutting code in `core/`; static config in `config/`; routing in `routing/`.
- Lowercase, singular layer folder names (`view_model`, not `viewModels`).

---

## Code style

- Comments and docs in **English**; on-screen copy in **Indonesian**.
- Early returns over nested `if`.
- Max ~200 lines per file — split if larger.
- No commented-out code.
- No `print()` — use `debugPrint()`.
- `const` constructors wherever possible.
- Named parameters for constructors with more than 2 parameters.

---

## State management (Riverpod)

- Use the modern `Notifier` / `AsyncNotifier` API — **not** the legacy `StateNotifier`.
- Mutable screen state → `NotifierProvider`. Async read-only → `AsyncNotifierProvider` /
  `FutureProvider`.
- Providers are colocated with what they provide. **Manual providers only** — there is no
  build_runner/codegen in this project (keeps the toolchain simple for a small app).

---

## CLI commands

```bash
flutter pub get                     # install dependencies
flutter run                         # run the app
flutter analyze                     # lint + static analysis (REQUIRED before commit)
flutter test                        # all tests
dart format .                       # format code
flutter pub outdated                # check for dependency updates
```

**Definition of done:** `flutter analyze` is clean AND `flutter test` is green.

---

## <a id="testing"></a>Testing

- Mirror `lib/` under `test/` with a `_test.dart` suffix
  (e.g. `test/ui/core/widgets/app_button_test.dart`).
- Prefer fast, deterministic widget tests for shared widgets and unit tests for view
  models / use cases (override providers with `ProviderScope(overrides: [...])`).
- Avoid full-app boot tests that depend on the splash timer + secure storage; they are flaky
  under fake-async. Test smaller units instead — that is why the sample test targets
  `AppButton` rather than the whole `App`.

---

## Lint

`analysis_options.yaml` uses `package:flutter_lints`. One exception is enabled:
`prefer_initializing_formals: false`. Don't disable other rules project-wide; prefer a
per-line `// ignore:` when truly necessary.

---

## Git

- Working branch: `develop`. Main branch: `main`.
- Clear, atomic commits. Don't commit directly to `main`.
- Ensure `flutter analyze` is clean before committing.
