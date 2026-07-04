# 03 — Conventions

See also [CLAUDE.md](../CLAUDE.md) — the source of truth for the strict rules. Quick reference here.

---

## Naming

| Thing | Rule | Example |
|---|---|---|
| Files | `kebab-case` | `toys-page.tsx`, `use-auth.ts` |
| Component | `PascalCase` | `ToysPage`, `Sidebar` |
| Hook | `useX` | `useAuth` |
| API module | `<route>-api.ts` exporting `<route>Api` | `toysApi.list()` |
| Types file | `<route>-types.ts` / `types.ts` | `toys-types.ts` |
| Interface / type | `PascalCase` | `Toy`, `LoginResponse`, `PageResponse<T>` |
| Route path | constant in `routes/paths.ts` | `paths.toys` |

---

## Folder layout

- `pages/<route>/` — a routed screen + its own `-api.ts`/`-types.ts`.
- `features/<name>/` — cross-page domain logic + state (e.g. `auth/`).
- `components/ui/` — reusable primitives. `components/layout/` — app chrome.
- `lib/` — framework-agnostic helpers (api-client, query-client, token-storage, types, utils).
- Import via the `@/` alias, not deep relative paths.

---

## Data & state

- Server data → **TanStack Query** (`useQuery`/`useMutation`). Never fetch in `useEffect`, never
  store server data in `useState`.
- `queryKey` is an array and includes any params. Invalidate it after mutations.
- Local UI state (input value, modal open) → `useState`. Session → `useAuth()`.
- All HTTP goes through `apiClient`; endpoint URLs live only in `*-api.ts` modules.

---

## UI & styling

- Compose `components/ui` primitives; never raw `<button>`/`<input>`.
- Use semantic Tailwind tokens (`bg-background`, `text-muted-foreground`, `bg-primary`,
  `border-border`) — no hex literals. Merge classes with `cn()`.
- Every data view renders **loading / error / empty** via `LoadingState`/`ErrorState`/`EmptyState`.
- Icons from `lucide-react`.

---

## Forms

- `react-hook-form` + `zod` via `zodResolver`. Define the schema, infer the type
  (`z.infer<typeof schema>`), show `errors.<field>.message`, and surface server errors with
  `getApiErrorMessage`.

---

## Language

- UI copy: **Indonesian**. Code, comments, identifiers, commit messages: **English**.

---

## Types & API contract

- Keep TS types in sync with [`../../BACKEND/docs/04_API_REFERENCE.md`](../../BACKEND/docs/04_API_REFERENCE.md).
- Snake_case JSON fields stay snake_case in the type (e.g. `avatar_url`) — don't silently rename.
- `PageResponse<T>` in `lib/types.ts` mirrors the backend's paged list envelope.

---

## Definition of Done

`npm run build` passes (runs `tsc` type-check + `vite build`). No TypeScript errors, no unused
locals/params (both are enforced by `tsconfig.json`).
