# SPK Mainan

**Sistem Pendukung Keputusan** (Decision Support System) untuk domain **mainan** — membantu
memilih mainan terbaik menggunakan metode SPK (mis. SAW / WP / TOPSIS / AHP).

Monorepo berisi tiga bagian:

| Folder | Bagian | Stack | Status |
|--------|--------|-------|--------|
| [`BACKEND/`](BACKEND) | REST API | Spring Boot 3.4 · Java 21 | ✅ Fondasi + auth siap |
| [`MOBILE/`](MOBILE) | Aplikasi mobile | Flutter · Riverpod · go_router | ✅ Fondasi + auth (login masih mock) |
| [`FRONTEND/`](FRONTEND) | Web client | _(belum diputuskan)_ | ⬜ Belum dimulai |

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

Semua client bicara ke backend lewat REST di base path `/v1` dengan **JWT bearer auth**.
Kontrak API-nya didokumentasikan di
[`BACKEND/docs/04_API_REFERENCE.md`](BACKEND/docs/04_API_REFERENCE.md).

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
Arahkan ke backend: set `ApiConfig.baseUrl` di
[`MOBILE/lib/config/api_config.dart`](MOBILE/lib/config/api_config.dart) dan hapus blok
`MOCK AUTH` di `auth_view_model.dart`. Detail: [`MOBILE/README.md`](MOBILE/README.md).

### Frontend (web)
Belum dimulai — lihat [`FRONTEND/README.md`](FRONTEND/README.md).

---

## Dokumentasi

- **Backend** — aturan & panduan untuk developer/AI: [`BACKEND/CLAUDE.md`](BACKEND/CLAUDE.md)
  dan [`BACKEND/docs/`](BACKEND/docs) (context, arsitektur, recipes, konvensi, API, roadmap).
- **Mobile** — [`MOBILE/CLAUDE.md`](MOBILE/CLAUDE.md) dan [`MOBILE/docs/`](MOBILE/docs).

## Langkah berikutnya

1. 🔴 Definisikan domain SPK (metode + kriteria + alternatif).
2. 🔴 Sambungkan mobile ke backend (baseUrl + hapus mock auth).
3. 🔴 Bangun fitur pertama di backend — pakai generator: `cd BACKEND && scripts/new-feature.sh Criterion criteria`.
