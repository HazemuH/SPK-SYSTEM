# KIDORA — Handover Checklist (Production)

Status audit fitur menjelang handover + implementasi sisa fitur.
Dibuat: 2026-08-15.

## Ringkasan Audit

| Fitur | Backend | Web Admin | Status |
| :-- | :-- | :-- | :-- |
| Auth (login/logout/profil) | ✅ | ✅ | Selesai |
| Data Mainan (CRUD) | ✅ | ✅ | Selesai |
| Kategori (CRUD) | ✅ | ✅ | Selesai |
| Kriteria (CRUD) | ✅ | ✅ | Selesai |
| Profil Bobot | ✅ | ✅ | Selesai |
| Pairwise (AHP) | ✅ | ✅ | Selesai |
| Kalkulasi + Publish | ✅ | ✅ | Selesai |
| Hasil / Ranking | ✅ | ✅ | Selesai |
| **Laporan (report sederhana)** | ✅ | ✅ | Selesai (arsip sesi kalkulasi + dashboard summary) |
| Dashboard summary | ✅ | ✅ | Selesai |
| Aplikasi Mobile | ✅ (public API) | — | Selesai (APK production-ready) |
| **Manajemen User & Role** | ❌ | ❌ | **DIKERJAKAN** |
| **Ubah Profil & Password** | ❌ (hanya lihat) | ❌ (stub) | **DIKERJAKAN** |

## Keputusan Final (Final Decisions)

1. **Role tetap `{ADMIN, USER}`.** ADMIN = pengelola penuh; USER = akun staf non-admin
   (mobile login-less, jadi USER hanya relevan di web). Tidak menambah role baru demi
   menjaga cakupan handover tetap sempit.
2. **Manajemen user hanya untuk ADMIN** — endpoint `/users` dilindungi
   `@PreAuthorize("hasRole('ADMIN')")`. Menu "Pengguna" di web hanya tampil bila
   role = admin.
3. **Guardrail anti-terkunci (penting untuk produksi):**
   - Tidak bisa menghapus akun sendiri.
   - Tidak bisa menghapus atau menurunkan role ADMIN terakhir (mencegah kehilangan akses).
4. **Ubah password wajib verifikasi password lama.** Panjang minimal password 8 karakter.
5. **Ubah profil** (nama, email, avatar) tersedia untuk semua user yang login (endpoint
   `/auth/profile` PUT), berlaku untuk dirinya sendiri.
6. **Di luar cakupan handover** (didokumentasikan sebagai pengembangan berikutnya):
   verifikasi email, lupa password / reset via email, audit log. Tidak diperlukan untuk
   go-live besok.

## Langkah Implementasi

### A. Backend — Manajemen User & Role ✅
- [x] `user/dto/CreateUserRequest.java`, `user/dto/UpdateUserRequest.java`
- [x] `user/UserRepository` — tambah `existsByEmail`, `countByRole`
- [x] `user/UserService.java` — CRUD + guardrail (unik, self-delete, last-admin)
- [x] `user/UserController.java` — `/users` (list/create/update/delete), admin-only (`@PreAuthorize`)
- [x] `GlobalExceptionHandler` — tambah handler `AccessDeniedException` → 403 (sebelumnya 500)
- [x] Test: `UserControllerTest` (7 test: happy + 401/403/409/400)
- Catatan: tabel `users` sudah ada (V1) dengan kolom lengkap → **tidak perlu migration baru**.

### B. Backend — Profil & Password (self-service) ✅
- [x] `auth/dto/UpdateProfileRequest.java`, `auth/dto/ChangePasswordRequest.java`
- [x] `AuthService` — `updateProfile`, `changePassword` (verifikasi password lama)
- [x] `AuthController` — `PUT /auth/profile`, `POST /auth/change-password`
- [x] Test: `AuthSelfServiceTest` (profil, ganti password, wrong-current → 400)

### C. Frontend — Manajemen User ✅
- [x] `pages/users/users-api.ts`
- [x] `pages/users/users-page.tsx` (list + dialog create/edit/delete, role Select, guard hapus-diri)
- [x] `routes/paths.ts` + `routes/router.tsx` + `nav-config.tsx` + `sidebar.tsx` (menu "Pengguna" admin-only)

### D. Frontend — Settings (profil & password) ✅
- [x] Tab "Profil Saya": form edit nama/email + simpan (update sesi via `updateUser`)
- [x] Form ganti password (lama + baru + konfirmasi)

### E. Verifikasi ✅
- [x] `./mvnw test` **hijau** (semua test, termasuk 10 test baru user/auth)
- [x] `npm run lint` **bersih** + `npm run build` (tsc + vite) **sukses**
- [x] `npm run test` frontend **19 passed**
- [x] Alur keamanan diuji end-to-end via MockMvc (login→JWT→role guard→guardrail 409/403)

## Hasil Akhir

Semua fitur yang belum selesai (**Manajemen User & Role**, **Ubah Profil & Password**)
telah **selesai, teruji, dan lulus verifikasi**. Fitur lain (CRUD, kalkulasi AHP-SAW,
laporan, dashboard, mobile) sudah lengkap sebelumnya. **Siap handover.**

### Endpoint baru
| Endpoint | Metode | Akses | Fungsi |
| :-- | :-- | :-- | :-- |
| `/users` | GET/POST | ADMIN | List & buat pengguna |
| `/users/{id}` | PUT/DELETE | ADMIN | Ubah & hapus pengguna |
| `/auth/profile` | PUT | Login | Ubah profil sendiri |
| `/auth/change-password` | POST | Login | Ubah password sendiri |

### Catatan operasional untuk tim produksi
- Akun admin awal (seed): `admin / password123` — **WAJIB ganti password** setelah go-live
  (kini bisa lewat menu Pengaturan → Ubah Password).
- Guardrail mencegah lockout: admin terakhir tidak bisa dihapus/diturunkan; tidak bisa
  hapus akun sendiri.
- Menu "Pengguna" hanya muncul untuk role admin.
