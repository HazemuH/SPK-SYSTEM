# PLAN_MVP — Frontend (SPK Mainan Admin · AHP-SAW)

> Implementation plan derived from the Hi-Fi design in `../../design/ahp-toko-mainan/`.
> Design source for the web admin: `design/ahp-toko-mainan/project/hifi/` (`app.jsx`, `web-a/b/c.jsx`,
> `web-pairwise.jsx`, `tokens.css`, `ui.jsx`). This app = the **admin panel (login)** that manages
> data and publishes AHP-SAW results to the mobile app.
> Follow [CLAUDE.md](../CLAUDE.md); backend contract in [`../../BACKEND/docs/PLAN_MVP.md`](../../BACKEND/docs/PLAN_MVP.md).

Last updated: **2026-07-05**

---

## 1. Scope

Recreate the **web admin** (13 screens) pixel-faithfully on our existing React 19 + Vite + TS +
Tailwind + TanStack Query skeleton. Auth already works against `/v1`. Everything else is new pages
+ a matching design system.

Screens (from `app.jsx` NAV + `web-*.jsx`):
1. **Login** — split hero (indigo gradient) + form (`web-a.jsx`). *(Adapt existing login-page.)*
2. **Dashboard** — 4 metric cards, riwayat kalkulasi table, donut (category dist), Top-5 bar chart, CTA.
3. **Data Mainan** — searchable/filterable paginated table; add/edit **SlideOver** with 2 tabs
   (Info Umum + Nilai Kriteria 1–5); delete confirm.
4. **Kategori** — 2-col cards grid, count per category, add/edit slideover, delete confirm.
5. **Kriteria AHP** — profile picker, weights table (weight bars, benefit/cost badges, Σ%),
   Consistency Check card (CR/λmax/CI/RI), 3-level hierarchy card.
6. **Profil Bobot** — scenario cards (top-weighted criteria chips, CR badge), add profile slideover.
7. **Pairwise Comparison** — 10×10 Saaty heatmap matrix (upper triangle editable, reciprocal auto),
   "Hitung Bobot & CR" → results (weight vector + CR panel).
8. **Kalkulasi & Publikasi** — stepper (Pre-check → Sintesis → Publikasi); pre-check list; calc
   loading overlay; per-scenario result cards; publish CTA.
9. **Hasil Kalkulasi** — profile picker, session meta, winner card, global ranking table, row →
   detail modal with **radar chart** + normalized `r_ij` bars.
10. **Laporan** + **Detail Laporan** — session archive cards (date filter) → detail (reuses Hasil).
11. **Pengaturan** — tabs: Profil Saya, Keamanan (change password), Tentang (method explainer).
12. **404**.

---

## 2. Design system (adapt our tokens to the design)

From `tokens.css`. Update `src/index.css` + `tailwind.config.js` to match:

- **Primary (indigo)** `#4F46E5` · **Accent (amber)** `#F59E0B` · success `#10B981` · danger `#EF4444`
  · info `#3B82F6` · violet `#8B5CF6`.
- **Font**: Plus Jakarta Sans (add as a self-hosted `@font-face`, not a CDN).
- Surfaces/text/border tokens + **dark mode** palette (design ships a full dark theme — wire a theme
  toggle now; our tokens already support `.dark`).
- Radii scale (6/10/14/20), shadows (xs–xl), layout vars (sidebar 256/70, topbar 64).

New/extended UI primitives in `components/ui/` (design → our shadcn-style):
`Button` (variants pr/se/df/gh/dg/ok + sizes), `Badge` (df/pr/ok/ng/wn/bl/vl), `Card`, `Table`,
`Input`/`Field` (prefix/suffix/icon), `Select`, `Toggle`, `SlideOver`, `Modal`, `Confirm`,
`Stepper`, `Pager`, `WeightBar`, `ProgressBar`, `Spinner`, `Avatar`, `Tabs`, `EmptyState` (have).
Charts: `DonutChart`, `HBarChart`, `RadarChart`, and the **pairwise Heatmap** (see `ui.jsx` for the
canvas/SVG implementations — port to small self-contained React components; no chart lib needed).

---

## 3. Structure (our conventions)

```
src/
├── features/auth/           # exists — reskin login-page to the split hero
├── components/
│   ├── ui/                  # primitives above
│   ├── charts/              # donut, hbar, radar, heatmap
│   └── layout/              # sidebar (10 nav items), topbar (breadcrumb, theme, bell, user menu)
├── pages/
│   ├── dashboard/
│   ├── toys/                # (exists as template) → rework to real /toys + scores slideover
│   ├── categories/
│   ├── criteria/
│   ├── weight-profiles/
│   ├── pairwise/
│   ├── calculation/
│   ├── results/            # Hasil + shared HasilContent
│   ├── reports/            # Laporan + detail
│   └── settings/
├── lib/                    # api-client (exists), types, utils
└── routes/                 # paths.ts + router.tsx (add all routes), nav-config.tsx (10 items)
```

Each page: `*-api.ts` (imports `apiClient`) + `*-types.ts` + `*-page.tsx`. Data via `useQuery`/
`useMutation`; invalidate on mutate. Generate scaffolds with `scripts/new-page.sh <Type> <route>`,
then wire to the real endpoint (Recipe C).

---

## 4. API wiring (see backend plan §4a)

| Page | Endpoints |
|---|---|
| Dashboard | `GET /dashboard/summary` |
| Data Mainan | `GET/POST/PUT/DELETE /toys`, `PUT /toys/{id}/scores` |
| Kategori | `GET/POST/PUT/DELETE /categories` |
| Kriteria | `GET /criteria`, `PUT /criteria/{id}`, `GET /weight-profiles` (picker) |
| Profil Bobot | `GET/POST/PUT/DELETE /weight-profiles` |
| Pairwise | `GET/PUT /weight-profiles/{id}/pairwise` (save → returns weights+CR) |
| Kalkulasi | `POST /calculations/precheck|run`, `POST /calculations/{id}/publish` |
| Hasil / Laporan | `GET /calculations`, `GET /calculations/{id}` |
| Pengaturan | `GET /auth/profile`, change-password endpoint |

Keep TS types in sync with the backend DTOs. Use the shared `apiClient` (JWT auto-attached).

---

## 5. Build order (phases)

1. **Design system** — port tokens (indigo/amber, Plus Jakarta Sans, dark mode), build/extend UI
   primitives + charts. Reskin **Login** and the **dashboard-layout** (sidebar 10 items + topbar).
2. **Reference CRUD** — Kategori, Kriteria (read + toggle), Data Mainan (table + slideover + scores).
   Wire to backend as those endpoints land.
3. **AHP screens** — Profil Bobot, Pairwise heatmap (the hardest component), Kriteria weights view.
4. **Calculation flow** — Kalkulasi stepper + publish; Hasil (ranking + radar detail modal).
5. **Reports + Settings + Dashboard summary** — Laporan list/detail, Pengaturan tabs, Dashboard data.
6. **Polish** — dark mode toggle, empty/loading/error states everywhere, responsive, tests for
   key pages (mirror `login-page.test.tsx`).

DoD per phase: `npm run lint` clean, `npm run test` green, `npm run build` passes.

---

## 6. Notes / decisions

- Design is **desktop-first admin** (fixed sidebar). Keep our SPA; add a mobile-collapse for the
  sidebar but desktop is the priority.
- The **Pairwise 10×10 heatmap** is the signature component — budget extra time; port the color
  scale (`hm-h1..h5` / `hm-l1..l5`) from `tokens.css`.
- Charts are hand-rolled in the design (`ui.jsx`) — reimplement as small SVG components, no external
  chart dependency (keeps the bundle lean and CSP-safe).
- Until backend endpoints exist, keep each page’s `*-api.ts` on mock data (like the current toys
  page) so UI can be built in parallel; swap to `apiClient` per endpoint as it lands.
- Copy is **Indonesian**; code/comments **English** (per CLAUDE.md).
