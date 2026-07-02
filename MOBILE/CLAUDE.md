# CLAUDE.md — Agent Instructions

## Project Overview
Flutter mobile app for a small product. Uses a **lightweight layered MVVM + Repository**
architecture, following the official Flutter app-architecture guidance
(https://docs.flutter.dev/app-architecture). Read this entire file before writing any code.

Comments and documentation are written in **English**. User-facing copy is in **Indonesian**
(the app's audience), so on-screen strings stay in Indonesian.

## Tech Stack
Flutter + Dart, go_router, flutter_riverpod (Notifier API), dio, flutter_secure_storage,
shared_preferences, intl, equatable.

## Architecture — Layered MVVM (keep it light)
Three layers. Dependencies point inward: `ui → domain (optional) → data`. The data layer
never imports the ui layer.

```
View (Widget)  →  ViewModel (Notifier)  →  Repository  →  Service (API)  →  ApiClient (dio)
                         └── UseCase (OPTIONAL, only for shared/complex logic) ──┘
```

- **UI layer** (`lib/ui/`): widgets (screens) + view models. View models hold screen state
  and call repositories. No business logic lives in widgets.
- **Domain layer** (`lib/domain/`): OPTIONAL. Add `use_cases/` only when logic is shared by
  multiple view models or is complex enough to test in isolation. Do NOT create a use case
  per action by default.
- **Data layer** (`lib/data/`): repositories (orchestrate), services (talk to the API), and
  models (data classes with `fromJson`/`toJson`).

### Rules
- NEVER call dio or a service directly from a widget or view model — go through a repository.
- NEVER put business logic in widgets.
- A single concrete `Repository` class is the default. Add an abstract interface only when you
  need multiple implementations or want to fake the whole repository in a test.
- Use ONE model class per concept (e.g. `User`). Do not split into separate entity/DTO classes
  unless the API shape genuinely diverges from what the UI needs.
- Errors use the single `AppException` hierarchy in `core/exceptions/`. Services/ApiClient throw
  them; view models catch with `on AppException` and `switch` on subtypes for messages.

## Folder Structure
```
lib/
├── main.dart
├── config/            # app_config.dart, api_config.dart (static configuration)
├── routing/           # router.dart, routes.dart (go_router)
├── core/              # cross-cutting, no business logic
│   ├── exceptions/    # app_exception.dart
│   ├── network/       # api_client.dart, network_info.dart
│   ├── storage/       # storage_keys.dart
│   └── utils/         # validators.dart, formatter.dart, extensions.dart
├── data/
│   ├── models/        # user.dart
│   ├── services/      # auth_api_service.dart
│   └── repositories/  # auth_repository.dart
├── domain/            # OPTIONAL: use_cases/, models/ (create only when needed)
└── ui/
    ├── core/
    │   ├── themes/    # colors.dart, spacing.dart, typography.dart, theme.dart
    │   └── widgets/   # shared widgets: app_button.dart, app_text_field.dart, ...
    └── <feature>/
        ├── view_model/  # <feature>_view_model.dart
        └── widgets/     # <feature>_screen.dart + feature-only widgets
```

## Folder & File Rules
- A new feature = a new folder under `ui/<feature>/` with `view_model/` and `widgets/`,
  plus repository/service/model in `data/` as needed.
- Shared widgets go in `ui/core/widgets/`. Feature-only widgets go in that feature's `widgets/`.
- Cross-cutting helpers go in `core/`. App/API constants go in `config/`.
- Folder names: lowercase, singular layer names (`view_model`, not `viewModels`).
- One public class per file is preferred; split files over ~200 lines.

## Widget Rules
- ALWAYS use `AppButton`, never `ElevatedButton`/`TextButton`/`OutlinedButton` directly.
- ALWAYS use `AppTextField`, never `TextFormField`/`TextField` directly.
- ALWAYS use `AppSnackbar` for feedback, never `ScaffoldMessenger` directly.
- ALWAYS use `AppColors` for colors, never `Colors.*` or hex literals.
- ALWAYS use `AppSpacing` for padding/margins, never hardcoded numbers.
- ALWAYS use `AppTypography` for text styles, never hardcoded `fontSize`.
- For data-loading screens, handle loading / error / empty in EVERY screen
  (`AppLoading`, `AppErrorView`, `AppEmptyState`).

## State Management (Riverpod)
- View models extend `Notifier<State>` / `AsyncNotifier<T>` (the modern API — do NOT use the
  legacy `StateNotifier`). Read dependencies via `ref` inside the notifier.
- Expose them with `NotifierProvider` / `AsyncNotifierProvider`.
- Read-only async data may use `FutureProvider` / `StreamProvider`.
- NEVER use `setState` for business state — use Riverpod. `setState` is fine for purely local
  widget UI (e.g. toggling password visibility).
- Provider definitions are colocated with what they provide (repository providers in the
  repository file, etc.). Manual providers only — there is NO build_runner / codegen.

## Navigation
- Use `go_router` only — never `Navigator.push` directly.
- Use path constants from `Routes` (`routing/routes.dart`), never hardcode strings.
- Pass data via `extra` / path params, never via constructors between screens.

## Naming Conventions
- Files & folders: `snake_case` (`login_view_model.dart`).
- Classes: `PascalCase` (`LoginScreen`).
- Variables/methods: `camelCase`.
- Constants: `camelCase` for instances/values; `SCREAMING_SNAKE` only for true compile-time
  primitive constants.
- View models: end with `ViewModel`; their provider ends with `ViewModelProvider`.
- Repositories: end with `Repository`. Services: end with `Service`/`ApiService`.
- Models: plain noun, no suffix (`User`, `Product`) — no `Model`/`Entity` suffix.
- Screens: end with `Screen`. Use cases (when present): end with `UseCase`.

## Adding a New Feature (checklist)
1. `data/models/<thing>.dart` — model with `fromJson`/`toJson`.
2. `data/services/<thing>_api_service.dart` — API calls + provider.
3. `data/repositories/<thing>_repository.dart` — orchestration + provider.
4. (Optional) `domain/use_cases/<action>_use_case.dart` — only if shared/complex.
5. `ui/<feature>/view_model/<feature>_view_model.dart` — Notifier + provider.
6. `ui/<feature>/widgets/<feature>_screen.dart` — handle loading/error/empty.
7. `routing/routes.dart` + `routing/router.dart` — add the route.
8. `config/api_config.dart` — add the endpoint constant.

## Adding a New API Call
1. Add the endpoint to `config/api_config.dart`.
2. Add the method to the relevant service, using the injected `apiClientProvider` dio — NEVER
   create a new `Dio` instance.
3. Surface it through the repository (catch/rethrow `AppException` as needed).
4. Call it from the view model (or a use case).

## Code Style
- Comments in English. Prefer early returns over nested conditionals.
- Max ~200 lines per file — split if larger.
- No commented-out code. No `print()` — use `debugPrint()`.
- `const` constructors wherever possible.
- Named parameters for constructors with more than 2 parameters.

## Definition of Done
`flutter analyze` is clean AND `flutter test` passes before any change is considered complete.
