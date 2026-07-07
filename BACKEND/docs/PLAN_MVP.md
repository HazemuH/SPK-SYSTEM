# PLAN_MVP — Backend (SPK Mainan · AHP-SAW)

> Implementation plan derived from the Hi-Fi design in `../../design/ahp-toko-mainan/`.
> Source of truth for the domain & algorithms: `design/ahp-toko-mainan/project/hifi/data.jsx`.
> Follow [CLAUDE.md](../CLAUDE.md) conventions; scaffold features with `scripts/new-feature.sh`.

Last updated: **2026-07-05**

---

## 1. Method (decided by the design): AHP-SAW hybrid

- **AHP** → weights of the **10 criteria** (only criteria are pairwise-compared, Saaty scale 1–9),
  one weight vector **per weight-profile**, validated by **CR ≤ 0.10**.
- **SAW** → synthesis of the **50 toys**: each toy rated **1–5** on the 9 benefit criteria + **price**
  as the 1 cost criterion → decision matrix → **normalize per criterion** → weighted sum.
  - Normalize: benefit `r = x/max(col)`, cost `r = min(col)/x` → all in `0..1`.
  - Score: `S_i(profile) = Σ_j ( w_j^profile × r_ij )`.
- **Kategori** = data attribute + filter only (NOT part of the AHP hierarchy). No pairwise between
  toys or categories.
- **Weight profiles (scenarios)** are the dynamic core: choosing a profile swaps the weight vector →
  the ranking changes. Toy ratings are identical across profiles.
- **Publish**: the admin computes rankings, then publishes a snapshot the mobile app reads
  (read-only). Mobile never computes AHP — it filters/sorts the published result.

Hierarchy: **Goal** (best toy) → **10 Criteria** → **50 Alternatives**.

---

## 2. Domain model (entities)

All entities extend `BaseEntity` (id + timestamps). Snake_case columns; Flyway per change.

| Entity | Key fields | Notes |
|---|---|---|
| `Category` | name, description | 8 seeded (edukatif, outdoor, puzzle, boneka, kendaraan, konstruksi, seni, olahraga) |
| `Criterion` | code, no, name, type(`BENEFIT`/`COST`), description, abbr, active | 10 fixed (9 benefit + 1 cost=harga). Seeded. |
| `WeightProfile` | code, name, short, icon, description, isDefault, active, cr, lambdaMax, ci, consistent | 5 seeded scenarios; the pairwise result. |
| `PairwiseEntry` | weightProfileId, rowCriterionId, colCriterionId, value(Saaty) | Upper triangle (i<j); reciprocal derived. Weights + CR computed from these. |
| `CriterionWeight` | weightProfileId, criterionId, weight(0..1) | Derived from pairwise; Σ=1 per profile. Cached for fast scoring. |
| `Toy` | name, categoryId, price, ageMin, ageMax, stock, active, description, tags(json/table) | 50 seeded. |
| `ToyScore` | toyId, criterionId, rating(1–5) | Only benefit criteria; price comes from `Toy.price`. |
| `CalculationSession` | code(no), runAt, weightProfileId, altCount, cr, lambdaMax, ci, consistent, bestToyId, published, publishedAt | One row per profile per run (or one run → 5 profile results). |
| `RankingEntry` | sessionId, toyId, rank, sawScore | Snapshot ranking (the published result). |
| `NormalizedScore` | sessionId(or global), toyId, criterionId, rValue(0..1) | Published normalized matrix `r_ij` for mobile radar/bars. |
| `User` | (exists) username, name, email, password, role | Admin only. |

> `tags` are free-form filter labels (mobile), outside AHP — a simple `toy_tags` table or a JSON column.

---

## 3. AHP-SAW engine (a `domain`/`service` package, well-tested)

Pure functions, no framework — put under `com.spkmainan.ahp` (or `calculation`), unit-tested hard.

1. **Weights from pairwise** (`AhpService.deriveWeights(matrix)`):
   - Build full n×n from upper triangle (reciprocals).
   - Priority vector via **geometric mean** row `w_i = (Π_j a_ij)^(1/n)`, then normalize Σ=1.
   - `λmax = avg( (A·w)_i / w_i )`, `CI = (λmax−n)/(n−1)`, `CR = CI / RI[n]`.
   - `RI[10] = 1.49` (table: {1:0,2:0,3:0.58,4:0.90,5:1.12,6:1.24,7:1.32,8:1.41,9:1.45,10:1.49}).
   - Return `{ weights, lambdaMax, ci, cr, consistent: cr<=0.10 }`.
2. **SAW normalize** (`SawService.normalize(toys, criteria)`): per criterion column, benefit `x/max`,
   cost `min/x` → `r_ij` matrix over **active** toys.
3. **Score & rank** (`SawService.rank(profile)`): `S_i = Σ w_j·r_ij`, sort desc, assign rank.
4. **Mobile helpers** (server-side, from published data): `recommend(answers)`,
   `catalogRanked(profile, sortCriterion)`, `compare(toyIds, profile)`,
   `strengthsWeaknesses(toy)`, `rankInCategory`, `nextBestAvailable`. All specced in `data.jsx`
   (recommend: hard-filter age+budget, soft-filter category, `prioritas` picks the profile via
   `PRIO_SCENARIO`).

---

## 4. Endpoints

### 4a. Admin (JWT-protected) — the web panel
```
Auth (exists)          POST /auth/login · POST /auth/logout · GET /auth/profile
Dashboard              GET  /dashboard/summary        # counts, category distribution, top5, recent sessions
Categories             GET/POST /categories · PUT/DELETE /categories/{id}
Criteria               GET  /criteria · PUT /criteria/{id}          # edit/toggle active (10 fixed)
Weight profiles        GET/POST /weight-profiles · GET/PUT/DELETE /weight-profiles/{id}
  pairwise             GET  /weight-profiles/{id}/pairwise          # matrix + current weights/CR
                       PUT  /weight-profiles/{id}/pairwise          # save matrix → recompute weights+CR
Toys                   GET  /toys?search=&categoryId=&page=         # paged (10/page)
                       POST /toys · PUT/DELETE /toys/{id}
                       PUT  /toys/{id}/scores                        # 9 benefit ratings 1–5
Calculation            POST /calculations/precheck                   # completeness/consistency checks
                       POST /calculations/run                        # SAW synth per profile → sessions+rankings
                       POST /calculations/{id}/publish               # publish snapshot to mobile
Reports                GET  /calculations · GET /calculations/{id}   # session list + detail ranking
```

### 4b. Public (NO auth) — the mobile app reads published data
```
Meta                   GET /public/meta                # categories, criteria, sort options, profiles
Profiles               GET /public/profiles            # published weight profiles (for switcher)
Catalog                GET /public/toys?sort=&categoryId=&inStock=&search=&profile=
Detail                 GET /public/toys/{id}?profile=  # score, global rank, cat rank, r_ij, why, next-best
Recommend              POST /public/recommend          # {usia,budget,tujuan,prioritas} → {primary,others,profile}
Compare                GET /public/compare?ids=1,2,3&profile=   # comparison table (best-per-criterion + totals)
Top                    GET /public/top?profile=balanced&limit=5  # home Top-5
```
Everything under `/public/**` returns only **published** data. If nothing is published yet →
empty/`404` with a clear message.

---

## 5. Security split (mobile no-login / web login)

In `SecurityConfig`: add `"/public/**"` to `PUBLIC_PATHS`; keep everything else authenticated.
Mobile calls only `/public/**`. Web calls the protected admin endpoints with the JWT. CORS already
allows configured origins.

---

## 6. Build order (phases)

1. **Foundation & auth split** — add `/public/**` to public paths; seed the demo admin (exists).
2. **Reference data** — `Category`, `Criterion` entities + CRUD + Flyway seed (8 cats, 10 criteria).
3. **Toys** — `Toy` + `ToyScore` CRUD (`scripts/new-feature.sh Toy toys`, then add scores + filters);
   Flyway seed the 50 toys from `data.jsx`.
4. **AHP engine** — `WeightProfile` + `PairwiseEntry` + `CriterionWeight`; `AhpService` (weights+CR);
   pairwise GET/PUT; seed 5 profiles. **Unit tests** against the CR/λmax values in `data.jsx`.
5. **SAW + calculation** — `SawService`; `/calculations/precheck|run`; sessions + rankings + normalized
   matrix. Verify top-1 per profile matches the design’s `SESSIONS` best.
6. **Publish + public API** — snapshot/publish; implement all `/public/**` (recommend/catalog/compare/
   detail/top/meta/profiles) reusing the engine.
7. **Dashboard summary** endpoint. Reports list/detail.

Definition of done per phase: `./mvnw test` green; new feature has happy + failure tests.

---

## 7. Seed data

Port from `design/ahp-toko-mainan/project/hifi/data.jsx`: `CATEGORIES` (8), `CRITERIA` (10),
`SCENARIOS_DEF` (5 profiles + weights + cr/λ/ci), `TOY_NAMES`+`scoreFor()` (50 toys with 1–5 ratings).
Put in Flyway migrations (or a `DataInitializer` for dev). Keep the exact ids/codes so FE/mobile match.

---

## 8. Open questions (confirm with user)

- Store the raw pairwise matrix and recompute weights server-side (recommended), or let admin enter
  weights directly? Design implies **pairwise → computed** (there’s a Pairwise screen).
- One calculation "run" = all 5 profiles at once (design shows 5 results per run) → model
  `CalculationSession` as run-level with per-profile ranking sets. Confirm.
- Roles: only `Admin` appears. Keep single role for MVP.
- Toy images: design uses no images (icon/initials only) — skip uploads for MVP.
