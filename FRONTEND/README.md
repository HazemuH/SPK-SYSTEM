# SPK Mainan — Frontend (Admin Web)

Management web client for the SPK Mainan (toy decision-support) system: login, manage toys &
criteria, configure the SPK method/formulas, and view rankings. Talks to the Spring Boot API in
[`../BACKEND`](../BACKEND) (`/v1`, JWT).

**Stack:** React 19 · Vite · TypeScript · Tailwind + shadcn-style UI · TanStack Query · React
Router v6 · axios · react-hook-form + zod.

## Requirements
- Node 20+ and npm
- The backend running (for real login) — see [`../BACKEND/README.md`](../BACKEND/README.md)

## Run (dev)
```bash
cp .env.example .env         # set VITE_API_BASE_URL (default http://localhost:8080/v1)
npm install
npm run dev                  # http://localhost:5173
```
Login with the backend's demo user: **admin** / **password123** (start the backend first).

## Build & quality
```bash
npm run build                # tsc type-check + vite production build → dist/
npm run preview              # serve the built app
npm run lint                 # ESLint (flat config: ts + react-hooks + react-refresh)
npm run format               # Prettier --write on src
```

## Scaffold a new management page (fast path)
```bash
scripts/new-page.sh Criterion criteria
```
Generates a searchable table page (types + api + page), then prints the 3 lines to register it
(`paths.ts`, `nav-config.tsx`, `router.tsx`). See [docs/02_DEVELOPMENT_GUIDE.md](docs/02_DEVELOPMENT_GUIDE.md).

## Structure
```
src/
├── lib/            # apiClient (axios+JWT), query-client, token-storage, types, utils(cn)
├── components/
│   ├── ui/         # Button, Input, Card, Table, Badge, Loading/Error/Empty states
│   └── layout/     # dashboard-layout, sidebar, topbar, nav-config
├── features/auth/  # login, session context, protected route
├── pages/          # dashboard-page + toys/ (management-table template)
└── routes/         # paths.ts, router.tsx
```

## Docs
- Agent/dev rules: [CLAUDE.md](CLAUDE.md)
- [docs/00_PROJECT_CONTEXT.md](docs/00_PROJECT_CONTEXT.md) · [01_ARCHITECTURE](docs/01_ARCHITECTURE.md) ·
  [02_DEVELOPMENT_GUIDE](docs/02_DEVELOPMENT_GUIDE.md) · [03_CONVENTIONS](docs/03_CONVENTIONS.md)

## Notes
- Toys data is **mocked** until the backend exposes `/toys` — swap the mock in
  `src/pages/toys/toys-api.ts` for the real `apiClient` call (Recipe C in the dev guide).
- API contract with the backend: [`../BACKEND/docs/04_API_REFERENCE.md`](../BACKEND/docs/04_API_REFERENCE.md).
