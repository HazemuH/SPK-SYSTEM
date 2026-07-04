# 01 — Architecture

React SPA, **feature/page-first**. Read [CLAUDE.md](../CLAUDE.md) first — it has the strict rules;
this explains the *why* and the data flow.

---

## Layers

```
Component (page / ui)
   │  useQuery / useMutation (TanStack Query)
   ▼
<route>-api.ts  ── the only place with endpoint URLs ──▶ apiClient (axios, singleton)
   │                                                        │  request: attach JWT
   ▼                                                        │  response: 401 → clear + /login
Backend REST (/v1, JWT)
```

- **Components** render UI and read/trigger data via hooks. No `fetch`/`axios` in components.
- **`*-api.ts` modules** own the endpoint URLs and return typed data. They import the shared
  `apiClient`. This keeps the network surface in one layer per feature.
- **`apiClient`** (`lib/api-client.ts`) is the single axios instance: injects the bearer token,
  and on 401 clears the session and redirects to `/login`.
- **TanStack Query** owns server state (caching, loading/error, refetch, invalidation). We do not
  keep server data in React state or fetch in `useEffect`.

### features/ vs pages/
- `pages/<route>/` — a routed screen and any data access it alone uses (`toys/`).
- `features/<name>/` — domain logic shared across pages, incl. React context/state (`auth/`).

---

## Session & auth flow

1. `AuthProvider` (`features/auth/auth-context.tsx`) mounts. If a token exists in
   `tokenStorage` (localStorage), it restores the user via `/auth/profile`; a bad token is cleared.
2. `ProtectedRoute` blocks unauthenticated access and redirects to `/login`; it shows a loading
   state while the session is being restored.
3. `login()` posts credentials, stores the JWT, sets the user. Every subsequent request carries
   the token via the `apiClient` request interceptor.
4. A `401` anywhere → interceptor clears the token and sends the user to `/login`.

---

## Routing

- `routes/paths.ts` — all path constants (import these, never hardcode).
- `routes/router.tsx` — `createBrowserRouter`: public `/login`, then a `ProtectedRoute` →
  `DashboardLayout` → child pages. Add new pages as children here.
- `components/layout/nav-config.tsx` — sidebar items; add an entry per management page.

---

## Styling

- Tailwind + CSS variables (shadcn tokens) defined in `src/index.css`. Colors are referenced as
  semantic tokens (`bg-background`, `text-muted-foreground`, `bg-primary`), so re-theming = editing
  the variables. Dark mode via a `.dark` class on a root element (tokens already defined).
- Primitives live in `components/ui`; compose them, use `cn()` to merge classes.

---

## Deliberately NOT included (keep it lean)

- No Redux/Zustand — TanStack Query + a small auth context cover it. Add a store only if real
  cross-page client state appears.
- No shadcn CLI dependency — primitives are hand-rolled and self-contained.
- No SSR/Next.js — a static SPA talking to a separate API is the right weight here.
Introduce these only when a concrete need justifies the complexity.
