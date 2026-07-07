# PLAN_MVP — Mobile (ToyAdvisor · read-only, no login)

> Implementation plan derived from the Hi-Fi design in `../../design/ahp-toko-mainan/`.
> Design source for mobile: `design/ahp-toko-mainan/project/hifi/mobile.jsx` + `mobile2.jsx`
> (+ `tokens.css`). This app = the **public, login-less** consumer app ("ToyAdvisor") that only
> **displays & filters** the AHP-SAW results the admin published.
> Follow [CLAUDE.md](../CLAUDE.md); backend contract in [`../../BACKEND/docs/PLAN_MVP.md`](../../BACKEND/docs/PLAN_MVP.md).

Last updated: **2026-07-05**

---

## 1. Scope

Read-only Flutter app, **no login** (already login-less). It reads only backend `/public/**`
endpoints. The heavy lifting (AHP-SAW) is done server-side; the app filters/sorts/compares the
published result and visualizes it.

Three entry modes from a Home hub + shared detail:
1. **Rekomendasi untuk Saya** — a 4-step preference quiz → process animation → ranked result.
2. **Jelajah Katalog** — full ranked catalog with sort-by-criterion + category/stock/search filters.
3. **Bandingkan Mainan** — pick 2–4 toys → side-by-side criteria table with a live profile switcher.
Plus **Detail Mainan** (score, global/category rank, "why", normalized `r_ij` radar+bars, next-best
if out of stock).

---

## 2. Screens (from `mobile.jsx` / `mobile2.jsx`)

| Screen | File ref | Key content |
|---|---|---|
| **Home hub** | `MobHome` | Indigo gradient header, 3 mode cards, Top-5 list (score bars), CR footer |
| **Preference quiz** | `MobPref` + `QUIZ` | 4 steps: usia (hard), budget (hard), tujuan (category soft), prioritas (picks profile). Progress bar, option cards |
| **Process** | `MobProses` | Animated 4-step "menyaring hasil AHP", then → result |
| **Recommendation** | `MobReko` | Winner hero (amber), preference summary chips, primary list + "rekomendasi lain" |
| **Catalog** | `MobKatalog` | Search, sort dropdown (6 opts), category select, in-stock toggle, ranked list ("✓ unggul <crit>") |
| **Compare** | `MobCompare` | Phase 1 pick 2–4; phase 2 sticky criteria table, profile switcher (live), winner banner |
| **Detail** | `MobDetail` | 3 stat tiles (SAW score / global rank / category rank), "Kenapa" strengths/weaknesses, radar + `r_ij` bars, next-best card |

Navigation is a simple in-app stack (design uses a `screen` state machine + `go(screen, arg)`);
implement with `go_router` routes: `/`, `/rekomendasi` (quiz), `/rekomendasi/hasil`, `/katalog`,
`/banding`, `/mainan/:id`. Pass args via `extra`/path params (per CLAUDE.md).

---

## 3. Design system (adapt tokens)

From `tokens.css` — update `ui/core/themes/`:
- **Primary indigo `#4F46E5`** (currently `#2563EB`), **accent amber `#F59E0B`**, success/danger/
  info/violet as in the design. Category colors map (`CAT_COLOR`).
- **Font Plus Jakarta Sans** (add to `assets/fonts/` + `pubspec.yaml`).
- **Dark mode** palette (design ships one) — wire the theme toggle in the Home header.
- Gradients per screen header (home indigo, reko amber, katalog teal, compare amber).

New shared widgets (`ui/core/widgets/`): `ScoreBar`, `CatBadge` (category-colored), `StockBadge`,
`RadarChart` (custom painter), option/quiz cards, comparison table cell. Reuse existing `AppCard`,
`AppLoading`, `AppErrorView`, `AppEmptyState`, `AppButton`, `Formatter.formatCurrency` (rupiah).

---

## 4. Data layer (rework current `toy` slice → real domain)

Replace the placeholder `Toy` with the real shape + a public API service (mock first, swap to real):

```
data/models/toy.dart            # id, nama, kategori, categoryId, harga, usia_min/max, tags, stok, aktif, deskripsi
data/models/ranked_toy.dart     # rank, sawScore, toy, r_ij map, catRank, strengths/weaknesses
data/models/weight_profile.dart # id, nama, short, icon, cr
data/services/public_api_service.dart   # GET /public/... (mock now)
data/repositories/catalog_repository.dart
```

Endpoints consumed (all public, see backend plan §4b):
`GET /public/top`, `POST /public/recommend`, `GET /public/toys` (catalog), `GET /public/toys/{id}`
(detail), `GET /public/compare`, `GET /public/profiles`, `GET /public/meta`.

View models: `AsyncNotifier` per screen (`HomeViewModel`, `RecommendationViewModel`,
`CatalogViewModel`, `CompareViewModel`, `ToyDetailViewModel`). Quiz answers + compare selection are
local UI state until submitted.

---

## 5. Build order (phases)

1. **Design system** — port indigo/amber tokens + Plus Jakarta Sans + dark mode; build `ScoreBar`,
   `CatBadge`, `StockBadge`. Rework `HomeScreen` into the hub (3 modes + Top-5) using mock data.
2. **Catalog** — list + sort/filter/search; `ToyDetail` (stat tiles + why + `r_ij` bars). Add
   `RadarChart` painter.
3. **Recommendation flow** — quiz (4 steps) → process animation → result (primary/others). Wire the
   `recommend` answers → `POST /public/recommend`.
4. **Compare** — pick 2–4 → criteria table with profile switcher (live re-rank) + winner banner.
5. **Wire to backend** — swap each service from mock to `apiClient` on `/public/**` as endpoints
   land; set `ApiConfig.baseUrl` (Android emu `http://10.0.2.2:8080/v1`).
6. **Polish** — loading/error/empty on every screen, pull-to-refresh, theme toggle, widget tests
   (mirror `home_screen_test.dart`) for Home, Catalog, Detail.

DoD per phase: `flutter analyze` clean, `flutter test` green.

---

## 6. Notes / decisions

- **No login, read-only** stays true — the app must degrade gracefully when nothing is published
  yet (empty state: "Belum ada hasil dipublikasikan").
- The profile switcher in Compare re-ranks **live** — fetch published profiles + normalized matrix
  once, recompute `S_i = Σ w_j·r_ij` client-side for instant switching (data is small), OR call
  `/public/compare?profile=`. Prefer client-side recompute for snappiness (matches the design).
- Server owns AHP-SAW; the app never runs pairwise/CR. It may recompute SAW weighted-sum from the
  published `r_ij` + profile weights for interactivity only.
- Copy **Indonesian**; code/comments **English**.
- Keep the phone-only layout (portrait); this is a handheld consumer app.
