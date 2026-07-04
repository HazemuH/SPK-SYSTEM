# SPK Mainan

**Sistem Pendukung Keputusan** (Decision Support System) untuk domain **mainan** — membantu
memilih mainan terbaik menggunakan metode SPK (mis. SAW / WP / TOPSIS / AHP).

Monorepo berisi tiga bagian:

| Folder | Bagian | Stack | Status |
|--------|--------|-------|--------|
| [`BACKEND/`](BACKEND) | REST API | Spring Boot 3.4 · Java 21 | ✅ Fondasi + auth siap |
| [`MOBILE/`](MOBILE) | Aplikasi mobile | Flutter · Riverpod · go_router | ✅ Read-only, tanpa login (Home rekomendasi, data mock) |
| [`FRONTEND/`](FRONTEND) | Admin web | React 19 · Vite · TS · Tailwind | ✅ Fondasi + auth + template tabel |

> Domain SPK-nya (kriteria, alternatif, bobot, metode scoring, ranking) **belum didefinisikan** —
> tentukan dulu sebelum membangun fitur inti.

---

## Arsitektur singkat

```
┌───────────┐        ┌───────────┐        ┌──────────────────┐
│  MOBILE   │        │ FRONTEND  │        │     BACKEND      │
│ (Flutter) │──────▶ │  (web)    │──────▶ │ Spring Boot API  │──▶ PostgreSQL / H2
└───────────┘  /v1   └───────────┘  /v1   │  JWT · Flyway    │
                                          └──────────────────┘
```

Semua client bicara ke backend lewat REST di base path `/v1`. **Web login (JWT)** untuk
manajemen; **mobile read-only tanpa login** (hanya endpoint publik). Kontrak API-nya
didokumentasikan di [`BACKEND/docs/04_API_REFERENCE.md`](BACKEND/docs/04_API_REFERENCE.md).

---

## Cara menjalankan

### Backend (Spring Boot)
```bash
cd BACKEND
./mvnw spring-boot:run          # jalan di http://localhost:8080/v1 (H2, tanpa setup)
```
Demo user: `admin` / `password123`. Swagger: `http://localhost:8080/v1/swagger-ui.html`.
Detail: [`BACKEND/README.md`](BACKEND/README.md).

### Mobile (Flutter)
```bash
cd MOBILE
flutter pub get
flutter run
```
Read-only & tanpa login — langsung buka layar Rekomendasi Mainan. Untuk data nyata: set
`ApiConfig.baseUrl` di [`MOBILE/lib/config/api_config.dart`](MOBILE/lib/config/api_config.dart)
dan hapus blok mock di `toy_api_service.dart`. Detail: [`MOBILE/README.md`](MOBILE/README.md).

### Frontend (React admin)
```bash
cd FRONTEND
cp .env.example .env
npm install
npm run dev                     # http://localhost:5173
```
Login dengan demo user backend (`admin` / `password123`). Detail:
[`FRONTEND/README.md`](FRONTEND/README.md).

---

## Dokumentasi

- **Backend** — aturan & panduan untuk developer/AI: [`BACKEND/CLAUDE.md`](BACKEND/CLAUDE.md)
  dan [`BACKEND/docs/`](BACKEND/docs) (context, arsitektur, recipes, konvensi, API, roadmap).
- **Mobile** — [`MOBILE/CLAUDE.md`](MOBILE/CLAUDE.md) dan [`MOBILE/docs/`](MOBILE/docs).

## Langkah berikutnya

1. 🔴 Bangun endpoint domain di backend (kriteria, mainan, nilai) — pakai generator:
   `cd BACKEND && scripts/new-feature.sh Criterion criteria`.
2. 🔴 Sambungkan client: web (manajemen, login) & mobile (endpoint publik read-only).
3. 🟢 Putuskan metode SPK (SAW / murni / dll.) — hanya memengaruhi perhitungan di backend, bisa belakangan.
