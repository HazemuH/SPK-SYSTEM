# SPK Mainan — Mobile App

A Flutter app built with a **lightweight layered MVVM + Repository** architecture, following
the official Flutter app-architecture guidance. The repository is currently a **starter**: the
foundation is complete, business features are not built yet.

> 📌 **Start here.** If you (or an AI assistant) open this project with no prior context, read
> **[docs/00_PROJECT_CONTEXT.md](docs/00_PROJECT_CONTEXT.md)** first. It explains what is done,
> what is still mocked, and the next steps.

> 🌐 Docs and code comments are in **English**. On-screen copy is in **Indonesian** (the app's
> audience).

---

## 📚 Documentation

| Document | Contents |
|---|---|
| [00_PROJECT_CONTEXT.md](docs/00_PROJECT_CONTEXT.md) | **Current status** — what exists, what is mocked, gaps, next steps. Read first. |
| [01_ARCHITECTURE.md](docs/01_ARCHITECTURE.md) | Layers, data flow, responsibilities of each folder. |
| [02_DEVELOPMENT_GUIDE.md](docs/02_DEVELOPMENT_GUIDE.md) | Step-by-step recipes: add a feature, a screen, an API call. |
| [03_DESIGN_SYSTEM.md](docs/03_DESIGN_SYSTEM.md) | Color, spacing, typography tokens and the widget catalog. |
| [04_CONVENTIONS.md](docs/04_CONVENTIONS.md) | Naming, code style, CLI commands, testing, git. |
| [05_ROADMAP.md](docs/05_ROADMAP.md) | Feature backlog and a progress-tracking template. |
| [CLAUDE.md](CLAUDE.md) | Instructions for AI agents (the strict architecture rules). |

---

## 🚀 Getting Started

```bash
flutter pub get          # install dependencies
flutter run              # run on a device/emulator
flutter analyze          # static analysis & lints
flutter test             # run tests
```

### Prerequisites
- Flutter SDK (Dart `^3.12.1`, tested on Flutter 3.44.x)
- An Android emulator / iOS simulator / physical device

### Login (current mode)
Auth is still **mocked**: enter any non-empty username & password to get in. See
[docs/00_PROJECT_CONTEXT.md](docs/00_PROJECT_CONTEXT.md#3--what-is-still-mocked) for how to
switch to the real API.

---

## 🧱 Tech Stack

| Area | Package |
|---|---|
| State management | `flutter_riverpod` (Notifier API) |
| Routing | `go_router` |
| Networking | `dio` |
| Secure storage | `flutter_secure_storage` |
| Preferences | `shared_preferences` |
| Format / i18n | `intl` |
| Value equality | `equatable` |
| Images | `cached_network_image`, `flutter_svg` |

---

## 📁 Structure at a Glance

```
lib/
├── config/          # static app & API configuration
├── routing/         # go_router setup
├── core/            # cross-cutting: exceptions, network, storage, utils
├── data/            # models, services (API), repositories
├── domain/          # OPTIONAL use cases (added only when needed)
└── ui/              # feature-first views + view models, shared themes/widgets
```

Full rules and layer responsibilities → [docs/01_ARCHITECTURE.md](docs/01_ARCHITECTURE.md).
