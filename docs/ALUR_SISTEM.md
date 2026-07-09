# Alur & Cara Kerja Sistem — SPK Mainan (AHP-SAW)

> Dokumen ringkas untuk memahami **tujuan, arsitektur, metode, dan alur** sistem.
> Cocok dijadikan rujukan Bab "Perancangan Sistem" / "Analisis" pada skripsi.

---

## 1. Tujuan sistem

Membantu **pemilihan mainan anak terbaik** menggunakan Sistem Pendukung Keputusan (SPK)
dengan metode **hybrid AHP-SAW**. Sistem punya dua sisi pengguna:

- **Admin (toko)** → mengelola data & menghitung rekomendasi lewat **aplikasi web**.
- **Pengunjung/staf** → melihat & menyaring hasil rekomendasi lewat **aplikasi mobile** (tanpa login).

---

## 2. Arsitektur (3 komponen + database)

```
┌──────────────────┐        ┌──────────────────┐
│   WEB (admin)    │        │  MOBILE (publik) │
│  React + Vite    │        │     Flutter      │
│  — LOGIN         │        │  — TANPA LOGIN   │
└───────┬──────────┘        └────────┬─────────┘
        │  REST /v1 (JWT)            │  REST /v1/public (tanpa auth)
        └──────────────┬─────────────┘
                       ▼
             ┌────────────────────┐
             │  BACKEND (Java)    │  ← "otak" & sumber kebenaran
             │  Spring Boot       │     - simpan data
             │  Mesin AHP-SAW     │     - hitung bobot & ranking
             └─────────┬──────────┘     - sediakan API
                       ▼
             ┌────────────────────┐
             │   PostgreSQL       │  (H2 in-memory saat dev/test)
             └────────────────────┘
```

**Prinsip:** hanya **backend** yang menghitung AHP-SAW. Web & mobile hanya menampilkan/menginput —
tidak ada logika perhitungan di sisi klien. Ini menjaga satu sumber kebenaran.

---

## 3. Peran pengguna

| | Web Admin (login) | Mobile (tanpa login) |
|---|---|---|
| Kelola kategori & mainan | ✅ | ❌ (hanya lihat) |
| Beri nilai (rating 1–5) tiap mainan | ✅ | ❌ |
| Susun bobot kriteria (pairwise) | ✅ | ❌ |
| Jalankan kalkulasi & publish | ✅ | ❌ |
| Lihat ranking / rekomendasi | ✅ | ✅ |
| Kuis preferensi & bandingkan mainan | — | ✅ |

---

## 4. Model data (entitas utama)

- **Kategori** (8): atribut pengelompokan & filter (mis. Edukatif, Puzzle). *Bukan* bagian hirarki AHP.
- **Kriteria** (10): 9 *benefit* + 1 *cost* (Harga). Contoh benefit: Keamanan, Nilai Edukasi, Kualitas.
- **Profil Bobot** (5 skenario): tiap profil = satu vektor bobot hasil pairwise tervalidasi
  (mis. "Utamakan Keamanan", "Utamakan Harga"). Ini kunci hasil dinamis.
- **Mainan** (alternatif): punya harga (cost) + rating 1–5 untuk tiap kriteria benefit.
- **Sesi Kalkulasi**: arsip hasil ranking per profil untuk satu kali kalkulasi (bisa dipublish).

> **Semua data dikelola admin (tambah/edit/hapus)** lewat web: kategori, mainan, dan **kriteria**
> (benefit/cost). Kriteria **Harga** bersifat khusus & tetap (nilainya = harga jual, tak bisa
> dihapus). Skripsi ini memakai **10 kriteria default** sebagai ruang lingkup; di toko nyata admin
> bisa menambah kriteria baru. Setelah kriteria diubah → **jalankan ulang Pairwise** (bobot dihitung
> ulang) dan **beri nilai 1–5** kriteria baru pada tiap mainan.

---

## 5. Metode AHP-SAW

**Hirarki 3 level:** Tujuan (mainan terbaik) → 10 Kriteria → Alternatif (mainan).

### 5a. AHP — pembobotan kriteria
Dipakai **hanya untuk menimbang kriteria** (bukan alternatif).

1. Admin mengisi **matriks perbandingan berpasangan** antar kriteria (skala Saaty 1–9).
2. Bobot dihitung dengan **rata-rata geometris** tiap baris lalu dinormalisasi:
   `wᵢ = (∏ⱼ aᵢⱼ)^(1/n) / Σ`
3. **Uji konsistensi**:
   `λmax = rata-rata((A·w)ᵢ / wᵢ)` · `CI = (λmax − n)/(n − 1)` · `CR = CI / RI`
   Matriks valid bila **CR ≤ 0,10** (RI untuk n=10 adalah 1,49).

### 5b. SAW — sintesis alternatif
Dipakai untuk **menilai banyak alternatif** (50 mainan) secara skalabel.

1. Susun **matriks keputusan**: rating 1–5 (benefit) + harga (cost).
2. **Normalisasi per kriteria** (hasil 0..1):
   - benefit: `rᵢⱼ = xᵢⱼ / max(kolom)`
   - cost:    `rᵢⱼ = min(kolom) / xᵢⱼ`
3. **Skor akhir** per profil bobot: `Sᵢ = Σⱼ (wⱼ × rᵢⱼ)` → urutkan menurun = ranking.

### 5c. Kenapa AHP-SAW, bukan AHP murni?
AHP murni butuh pairwise antar **alternatif** juga. Untuk 50 mainan itu = 50×49/2 = **1.225
perbandingan** per kriteria — tidak praktis & rawan inkonsisten. Maka:
**AHP untuk bobot kriteria** (sedikit, butuh justifikasi) + **SAW untuk alternatif** (banyak,
cukup rating langsung). Kombinasi: bobot kredibel (teruji CR) + sintesis efisien.

---

## 6. Contoh perhitungan (bisa dipertahankan di sidang)

Misal **3 kriteria**: Keamanan, Edukasi, Harga(cost).

**AHP — matriks pairwise:**
```
          Keamanan  Edukasi  Harga
Keamanan     1        2        3
Edukasi     1/2       1        2
Harga       1/3      1/2       1
```
Rata-rata geometris → normalisasi → **bobot**:
`w = [Keamanan 0,540 · Edukasi 0,297 · Harga 0,163]`
Konsistensi: `λmax ≈ 3,009` → `CI ≈ 0,0045` → **CR ≈ 0,008 ≤ 0,10 → konsisten ✓**

**SAW — 2 mainan:**
| | Keamanan | Edukasi | Harga |
|---|---|---|---|
| Mainan A | 4 | 5 | Rp100.000 |
| Mainan B | 3 | 3 | Rp50.000 |

Normalisasi (benefit x/max, cost min/x):
| | Keamanan | Edukasi | Harga |
|---|---|---|---|
| A | 1,000 | 1,000 | 0,500 |
| B | 0,750 | 0,600 | 1,000 |

Skor SAW `Sᵢ = Σ wⱼ·rᵢⱼ`:
- **A** = 0,540·1,000 + 0,297·1,000 + 0,163·0,500 = **0,918**
- **B** = 0,540·0,750 + 0,297·0,600 + 0,163·1,000 = **0,746**

→ **Ranking: A (#1), B (#2).** Ganti profil bobot ⇒ bobot berubah ⇒ ranking bisa berubah.

---

## 7. Alur end-to-end (yang bikin terasa "produk nyata")

```
ADMIN (web)                                        MOBILE (publik)
────────────                                       ───────────────
1. Login
2. Kelola Kategori & Mainan (+ rating 1–5)
3. Susun Pairwise antar kriteria  ──► hitung bobot + CR (AHP)
      └─ CR ≤ 0,10? lanjut : perbaiki
4. Kalkulasi  ──► normalisasi + weighted sum (SAW) per profil → ranking
5. Publikasikan hasil  ─────────────────────────►  6. Menampilkan ranking terpublish
                                                      • Top rekomendasi
                                                      • Kuis preferensi → rekomendasi
                                                      • Jelajah katalog (sortir per kriteria)
                                                      • Bandingkan 2–4 mainan
```

### 7a. Bagaimana kuis mobile terhubung ke AHP-SAW

Kuis 4 langkah menerjemahkan preferensi awam menjadi parameter SPK:

| Langkah | Peran di metode |
|---|---|
| Usia | **filter keras** (mempersempit kandidat, bukan skoring) |
| Budget | **filter keras** |
| Tujuan | **filter lunak** (prioritaskan kategori) |
| **Paling penting?** | **memilih profil bobot** = memakai satu vektor bobot hasil **AHP** yang sudah tervalidasi (CR ≤ 0,10) |

Pertanyaan terakhir menampilkan **daftar profil bobot yang dipublish admin**; jawaban mengirim
**kode profil** langsung. Jadi pengguna **tidak menghitung bobot** (yang rawan inkonsisten) — ia
hanya **memilih skenario**, sedangkan pembobotannya tetap dari AHP. Menambah/menghapus profil di web
otomatis mengubah opsi kuis. Ini menjaga kerigoran metode sekaligus mudah dipakai orang awam.

---

## 8. Teknologi

| Komponen | Stack |
|---|---|
| Backend | Java 21, Spring Boot 3.4, Spring Security (JWT), Spring Data JPA, Flyway |
| Database | PostgreSQL (produksi/Docker) · H2 in-memory (dev/test) |
| Web admin | React 19, Vite, TypeScript, Tailwind, TanStack Query |
| Mobile | Flutter, Riverpod, go_router, Dio |
| Deploy | Docker Compose (PostgreSQL + backend + frontend/nginx) |

---

## 9. Cara menjalankan

```bash
# semua sekaligus (web + API + database)
docker compose up --build
#   Web admin : http://localhost:5173   (admin / password123)
#   API       : http://localhost:8080/v1
#   Mobile    : flutter run (emulator) → baseUrl http://10.0.2.2:8080/v1
```

Data awal (8 kategori, 10 kriteria, 5 profil, 50 mainan) **diisi otomatis** oleh seeder Java
(`backend/.../domain/DomainSeeder.java` + `DomainSeed.java`) saat pertama jalan.

---

## 10. Status implementasi (jujur — untuk direncanakan)

| Bagian | Status |
|---|---|
| AHP (pairwise → bobot → CR) | ✅ berjalan & teruji |
| SAW (normalisasi → weighted sum → ranking) | ✅ berjalan & teruji |
| CRUD data + kalkulasi + arsip laporan | ✅ |
| "Publish" sebagai **gerbang** ke mobile | ⚠️ **belum ditegakkan** — mobile kini baca data *live*; idealnya mobile hanya baca snapshot terpublish sampai admin publish ulang |
| Blokir profil dengan CR > 0,10 dari kalkulasi | ⚠️ baru diperingatkan, belum diblokir keras |

> Dua item ⚠️ di atas adalah penyempurnaan yang membuat alur benar-benar sesuai narasi SPK
> (compute → validasi → publish → konsumsi). Direkomendasikan dikerjakan sebelum sidang.
