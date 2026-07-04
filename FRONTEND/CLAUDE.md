# CLAUDE.md — Agent Instructions (Frontend)

## Project Overview
Admin/management web client for **SPK Mainan** (a toy decision-support system): manage toys,
criteria, SPK methods/formulas, and view rankings — all behind a login. Talks to the Spring Boot
API in `../BACKEND` (`/v1`, JWT bearer). **Read this entire file before writing any code.**

UI copy is in **Indonesian** (the audience); code, comments, and identifiers are in **English**.

## Tech Stack
React 19 + Vite + TypeScript. Tailwind CSS + shadcn-style components (hand-rolled in
`components/ui`). TanStack Query (server state), React Router v6, axios, react-hook-form + zod
(forms/validation), lucide-react (icons). Package manager: npm.

## Fastest path — scaffold, then fill in (vibe coding)
Don't hand-write a list page. Generate one and edit the columns:

```bash
scripts/new-page.sh Criterion criteria      # → src/pages/criteria/*, component CriteriaPage
```

Then register it in the 3 places the script prints (`paths.ts`, `nav-config.tsx`, `router.tsx`).

Reusable building blocks — always use these, don't reinvent:
- `lib/api-client.ts` → the single configured `apiClient` (axios). Attaches the JWT, handles 401.
  **Never create another axios instance.** Use `getApiErrorMessage(err)` for messages.
- `components/ui/*` → `Button`, `Input`, `Label`, `Card`, `Table`, `Badge`, and the three data
  states `LoadingState` / `ErrorState` / `EmptyState`. Use `cn()` from `lib/utils` for classes.
- `features/auth/use-auth.ts` → `useAuth()` for the session (`user`, `login`, `logout`).
- `lib/types.ts` → `PageResponse<T>` matching the backend's paged list envelope.

## Folder Structure
```
src/
├── main.tsx, App.tsx            # bootstrap: QueryClient → AuthProvider → RouterProvider
├── config/env.ts               # typed VITE_* env access
├── lib/                        # api-client, query-client, token-storage, types, utils(cn)
├── components/
│   ├── ui/                     # design-system primitives (shadcn-style)
│   └── layout/                 # dashboard-layout, sidebar, topbar, nav-config
├── features/<feature>/         # cross-page domain logic (state/context). e.g. auth/
├── pages/<route>/              # a routed screen + its local api/types. e.g. toys/
└── routes/                     # paths.ts (path constants), router.tsx (route tree)
```

### Where does code go?
- **A new screen** = a folder under `pages/<route>/` with `<route>-page.tsx` (+ `-api.ts`,
  `-types.ts` if it has its own data). Use the generator.
- **Shared, cross-page logic** (auth, current-user, app-wide state) = `features/<name>/`.
- **Reusable visual component** = `components/ui/`. **Layout chrome** = `components/layout/`.
- **Anything talking to the backend** goes through an `*-api.ts` module that imports `apiClient`.

## Rules
- ALWAYS use the shared `apiClient`; never call `fetch`/`axios` directly in a component.
- ALWAYS fetch server data with TanStack Query (`useQuery`/`useMutation`), never in `useEffect`.
- After a mutation, `queryClient.invalidateQueries({ queryKey: [...] })` — don't hand-roll refetch.
- ALWAYS handle the three states on data views: loading, error, empty (use the `*State` components).
- ALWAYS use `components/ui` primitives + `cn()` + theme tokens (`bg-background`, `text-muted-foreground`, …).
  Never hardcode hex colors or use raw `<button>`/`<input>`.
- Route paths come from `routes/paths.ts` — never hardcode path strings.
- Validate forms with zod + react-hook-form (`zodResolver`).
- Keep secrets/config in `.env` (prefix `VITE_`); read via `config/env.ts`.

## Naming Conventions
- Files: `kebab-case` (`toys-page.tsx`, `use-auth.ts`). One main export per file.
- Components: `PascalCase` ending as appropriate (`ToysPage`, `Sidebar`).
- Hooks: `useX`. API modules: `<route>-api.ts` exporting a `<route>Api` object.
- Types/interfaces: `PascalCase` (`Toy`, `LoginResponse`). Path constants live in `paths`.
- Import with the `@/` alias (e.g. `@/components/ui/button`), not long relative paths.

## API Contract
Backend base URL `/v1`, JWT bearer. Endpoints and the `user`/error shapes are documented in
[`../BACKEND/docs/04_API_REFERENCE.md`](../BACKEND/docs/04_API_REFERENCE.md). Keep types in
`features/*/types.ts` / `pages/*/*-types.ts` in sync with it. Snake_case JSON fields stay
snake_case in the TS types (e.g. `avatar_url`).

## Definition of Done
Before any change is complete: `npm run lint` (ESLint) is clean, `npm run build` passes (`tsc`
type-check + `vite build`), and code is Prettier-formatted (`npm run format`). Prefer adding a page
via the generator so structure and states are correct by default.

Tooling: **ESLint 9** (flat config, `eslint.config.js`: typescript-eslint + react-hooks +
react-refresh) and **Prettier** (`.prettierrc.json`). Scripts: `lint`, `lint:fix`, `format`,
`format:check`, `typecheck`, `build`.

## Key References
- Architecture → [docs/01_ARCHITECTURE.md](docs/01_ARCHITECTURE.md)
- Recipes → [docs/02_DEVELOPMENT_GUIDE.md](docs/02_DEVELOPMENT_GUIDE.md)
- Conventions → [docs/03_CONVENTIONS.md](docs/03_CONVENTIONS.md)
- Status → [docs/00_PROJECT_CONTEXT.md](docs/00_PROJECT_CONTEXT.md)
