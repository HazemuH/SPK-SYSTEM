# 00 — Project Context (Frontend, Current Status)

> **Purpose:** enough context for anyone (human or AI) to continue frontend development from a
> blank session. **Update this file when a major status changes.**

Last updated: **2026-07-02**

---

## 1. What is this?

The admin/management web client for **SPK Mainan** (toy decision-support). It lets an admin log
in and manage the data behind the SPK: **toys** (alternatives), **criteria**, **methods/formulas**,
and view **rankings**. It calls the Spring Boot API in [`../../BACKEND`](../../BACKEND) at `/v1`
with JWT auth.

The concrete SPK domain (method, criteria, scoring) is **not yet defined** — see the roadmap in
[`../../BACKEND/docs/05_ROADMAP.md`](../../BACKEND/docs/05_ROADMAP.md).

---

## 2. What already exists ✅

- **Stack & build** — React 19 + Vite + TS, Tailwind + shadcn-style UI, TanStack Query, React
  Router v6, axios, react-hook-form + zod. `npm run build` is green (type-check + bundle).
- **Auth end-to-end** — login form (zod-validated) → `/auth/login`, JWT stored, session restored
  on reload via `/auth/profile`, logout, protected routes, auto-redirect on 401.
- **App shell** — sidebar + topbar dashboard layout with active-nav highlighting.
- **Dashboard page** — greeting + stat cards (placeholder numbers).
- **Toys page** — a working management **table template** (search + loading/error/empty states).
- **Design system** — `components/ui` primitives + theme tokens (light/dark ready via `.dark`).
- **Generator** — `scripts/new-page.sh` scaffolds a new management page.

---

## 3. What is mocked / not wired ⚠️

- **Toys data is mocked** in `pages/toys/toys-api.ts` (no `/toys` endpoint on the backend yet).
  Switch to the real API by uncommenting the `apiClient.get("/toys")` call shown there.
- **Dashboard stats are static** placeholders.
- **Add/Edit/Delete buttons** on the toys table are not wired to mutations yet.
- **Dark mode** tokens exist but there's no theme toggle UI yet (add `.dark` on `<html>`).

Auth is **real** — it needs the backend running (`../../BACKEND`, demo `admin`/`password123`).

---

## 4. Ready to develop features? 

**Yes.** Auth, routing, data-fetching, the design system, and a table template are in place.
To add a management screen: run `scripts/new-page.sh <Type> <route>`, register it (paths/nav/
router), then replace the mock with a real `apiClient` call once the backend endpoint exists.

| # | Action | Priority |
|---|---|---|
| 1 | Point `.env` `VITE_API_BASE_URL` at the running backend | 🔴 |
| 2 | Build backend SPK endpoints, then swap mocks → `apiClient` | 🔴 |
| 3 | Add criteria/methods pages via the generator | 🟡 |
| 4 | Wire create/edit/delete mutations on tables | 🟡 |
| 5 | Theme toggle (light/dark) | 🟢 |

---

## 5. How to run

```bash
cp .env.example .env      # set VITE_API_BASE_URL if not localhost:8080/v1
npm install
npm run dev               # http://localhost:5173
```
Login with the backend's demo user `admin` / `password123` (start the backend first).
