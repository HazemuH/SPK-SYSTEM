# 02 — Development Guide (Recipes)

Step-by-step recipes. Every example mirrors the existing **Auth** and **Toys** code — use them as
living templates. Read [01_ARCHITECTURE.md](01_ARCHITECTURE.md) and [CLAUDE.md](../CLAUDE.md) first.

---

## Recipe 0 — Generate a management page (the fast path)

```bash
scripts/new-page.sh Criterion criteria
```

You get `src/pages/criteria/` with `criteria-types.ts`, `criteria-api.ts` (mock), and
`criteria-page.tsx` (a searchable table with loading/error/empty states). Then register it in the
three places the script prints:

```ts
// 1) src/routes/paths.ts
criteria: "/criteria",

// 2) src/components/layout/nav-config.tsx  (import an icon from lucide-react)
{ label: "Kriteria", to: paths.criteria, icon: SlidersHorizontal },

// 3) src/routes/router.tsx  (inside the DashboardLayout children)
{ path: paths.criteria, element: <CriteriaPage /> },
```

Then edit `criteria-types.ts` columns, the table headers/cells in the page, and — when the backend
is ready — swap the mock in `criteria-api.ts` for a real call (Recipe C). `npm run build`.

---

## Recipe A — Add a page by hand (what the generator produces)

1. `src/pages/<route>/<route>-types.ts` — the row interface.
2. `src/pages/<route>/<route>-api.ts` — a `<route>Api` object importing `apiClient` (or mock).
3. `src/pages/<route>/<route>-page.tsx` — `useQuery` → render loading/error/empty → `Table`.
   Copy `pages/toys/toys-page.tsx`.
4. Register in `paths.ts`, `nav-config.tsx`, `router.tsx`.

---

## Recipe B — Fetch data (read)

```tsx
const { data, isLoading, isError, error, refetch } = useQuery({
  queryKey: ["toys"],
  queryFn: toysApi.list,
});

if (isLoading) return <LoadingState />;
if (isError) return <ErrorState message={getApiErrorMessage(error)} onRetry={() => void refetch()} />;
if (data.length === 0) return <EmptyState />;
```

Always render the three states. `queryKey` identifies the cache entry (use an array; include
params, e.g. `["toys", page]`).

---

## Recipe C — Point an api module at the real backend

In `<route>-api.ts`, replace the mock with the shared client:

```ts
import { apiClient } from "@/lib/api-client";
import type { PageResponse } from "@/lib/types";
import type { Toy } from "./toys-types";

export const toysApi = {
  async list(): Promise<Toy[]> {
    const { data } = await apiClient.get<PageResponse<Toy>>("/toys");
    return data.content;
  },
};
```

The JWT is attached automatically; a 401 redirects to login. No other change needed in the page.

---

## Recipe D — Mutations (create / update / delete)

```tsx
const queryClient = useQueryClient();

const createToy = useMutation({
  mutationFn: (payload: ToyInput) => toysApi.create(payload),
  onSuccess: () => queryClient.invalidateQueries({ queryKey: ["toys"] }),
});

// in a submit handler:
await createToy.mutateAsync(values);
```

Never manually refetch — invalidate the query key and TanStack Query refreshes it.

---

## Recipe E — A form (react-hook-form + zod)

Copy `features/auth/login-page.tsx`:

```tsx
const schema = z.object({ name: z.string().min(1, "Nama wajib diisi") });
type Values = z.infer<typeof schema>;
const { register, handleSubmit, formState: { errors, isSubmitting } } =
  useForm<Values>({ resolver: zodResolver(schema) });
```

Show `errors.<field>.message` under each `Input`, and a server error from `getApiErrorMessage`.

---

## Common pitfalls

- **Blank page / 401 loop** → backend not running or `VITE_API_BASE_URL` wrong. Check `.env`.
- **CORS error** → set the backend's `CORS_ALLOWED_ORIGINS` (dev default allows all).
- **Import not found** → use the `@/` alias (`@/components/ui/button`).
- **Stale list after create** → you forgot `invalidateQueries` with the right `queryKey`.
- **Type error on `import.meta.env`** → add the var to `src/vite-env.d.ts` and `config/env.ts`.
