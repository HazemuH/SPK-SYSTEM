# Desain — Gerbang Publish (Publish Gate) untuk SPK Mainan

**Tanggal:** 2026-07-12
**Status:** disetujui untuk implementasi
**Ruang lingkup paket ini:** plot hole #1 (gerbang publish) + #2 (validasi run) + #3 (hormati kriteria aktif).
**Ditunda ke paket berikutnya:** #4 sinyal "basi" + banner admin, cue "Diperbarui" & polish empty-state mobile, penghalusan bahasa kuis.

---

## 1. Latar & masalah (analisis system analyst)

SPK Mainan bernarasi **compute → validate → publish → consume**: admin menghitung AHP-SAW lalu
*mempublikasikan*, dan mobile *mengonsumsi hasil terpublish*. Analisis kode menemukan narasi ini
belum ditegakkan:

| # | Plot hole | Bukti kode | Status |
|---|---|---|---|
| 1 | **Gerbang publish palsu.** API publik menghitung *live* dari data terkini; tak pernah membaca `CalculationRun` terpublish. Edit admin yang belum dipublish langsung bocor ke mobile. | `CatalogService.snapshot()` membaca `catalog.activeToys()` | ditutup di paket ini |
| 2 | **Kalkulasi cacat lolos di backend.** `run()`/`publish()` tak menegakkan precheck; CR>0,10 atau mainan belum dinilai tetap bisa dipublish via API. (FE sudah mengunci tombol, backend belum.) | `CalculationService.run()` tanpa guard | ditutup di paket ini |
| 3 | **Kriteria non-aktif tetap dihitung.** Toggle "Aktif" kosmetik; engine memakai semua kriteria. | `DomainCatalog.criteria()` = `findAll`, tak filter `active` | ditutup di paket ini |
| 4 | **Tak ada sinyal "basi".** Setelah digerbang, edit admin diam-diam tak sampai ke mobile tanpa publish ulang. | belum ada | **ditunda** |

Tujuan: setelah paket ini, langkah **5 (publish) → 6 (mobile konsumsi)** benar-benar terhubung tanpa
lubang — layak dipresentasikan untuk skripsi *dan* dipakai toko nyata.

## 2. Keputusan desain (dari diskusi)

- **Yang dibekukan gerbang:** skor SAW, ranking, bobot, CR = hasil **terpublish** (beku). Atribut
  tampilan mainan (nama, harga, stok, gambar, deskripsi, tag) tetap **live** per `toy_id`.
- **Belum ada run terpublish:** seluruh layar mobile tampil kosong ("Hasil belum dipublikasikan").
- **Pendekatan A — bekukan matriks keputusan, hitung ulang saat baca.** Simpan **input** (bobot per
  profil + nilai mentah mainan) ke run; API publik menjalankan `SawEngine` atas input beku. Ini
  menjamin skor, r_ij, ranking, bobot, CR **konsisten by construction** dan mempertahankan
  `SawEngine` sebagai mesin tunggal (narasi sidang bersih).
- **Kriteria aktif:** hanya kriteria `active` yang ikut pairwise, bobot, normalisasi, skoring, dan
  daftar kriteria publik. Kriteria non-aktif = diarsipkan (dikecualikan di mana pun perhitungan).

## 3. Perubahan skema (Flyway `V4__publish_snapshot.sql`)

Dua tabel membekukan input keputusan tiap run:

**`calculation_weight`** — bobot AHP beku, per hasil-profil per kriteria:
```
id            BIGINT PK
result_id     BIGINT NOT NULL  → calculation_result(id)  (ON DELETE CASCADE)
criterion_code VARCHAR(50) NOT NULL
weight        DOUBLE NOT NULL
UNIQUE(result_id, criterion_code)
```
±5 profil × jumlah kriteria aktif = ±50 baris/run. Membekukan bobot (CR sudah di `calculation_result`).

**`calculation_toy_value`** — nilai mentah mainan beku (matriks keputusan), per run per mainan per kriteria:
```
id             BIGINT PK
run_id         BIGINT NOT NULL  → calculation_run(id)  (ON DELETE CASCADE)
toy_id         INT NOT NULL
criterion_code VARCHAR(50) NOT NULL
raw_value      DOUBLE NOT NULL   -- rating 1..5, atau harga untuk kriteria "harga"
UNIQUE(run_id, toy_id, criterion_code)
```
±jumlah mainan aktif × kriteria aktif = ±500 baris/run. Membekukan input SAW.

> Menyimpan **nilai mentah** (bukan r_ij ternormalisasi) karena normalisasi bergantung pada seluruh
> set; `SawEngine.normalize()` atas set beku menghasilkan r_ij yang selalu selaras dengan skor.

Entity JPA baru: `CalculationWeight` (child dari `CalculationResult`) dan `CalculationToyValue`
(child dari `CalculationRun`), mengikuti pola `RankingEntry` (extend `BaseEntity`, `@ManyToOne`).

## 4. Perubahan tulis (saat `run()` / `publish()`)

`CalculationService.run()`:
1. **Validasi dulu (#2):** panggil `precheck()`. Jika `!allOk` → lempar `BadRequestException`
   berisi item yang gagal (mis. "Ada profil dengan CR > 0,10"). Jadi run cacat mustahil dibuat.
2. Pakai **kriteria aktif** (`activeCriteria`) & **mainan aktif** (`activeToys`) untuk seluruh
   perhitungan (#3).
3. Selain `RankingEntry` (sudah ada), **persist**: `calculation_toy_value` (nilai mentah tiap
   mainan aktif × kriteria aktif) dan `calculation_weight` (bobot tiap profil × kriteria aktif).

`publish()` tetap: set satu run `published=true`, sisanya `false`. (Tak ada endpoint unpublish baru
di paket ini; publish run lain otomatis meng-unpublish yang lama.)

`runAndPublish()` (seeder) tetap jalan — data seed konsisten sehingga precheck lolos.

## 5. Perubahan baca (jalur API publik)

`CatalogService` beralih dari sumber **live** ke **run terpublish terbaru**.

- Repo baru: `CalculationRunRepository.findFirstByPublishedTrueOrderByPublishedAtDesc()`.
- `snapshot()` baru → membangun `PublishedSnapshot`:
  - Ambil run terpublish. **Kosong → `PublishedSnapshot.empty()`** (semua query balikan kosong).
  - Susun matriks nilai mentah dari `calculation_toy_value` → `SawEngine.normalize()` = r_ij beku.
  - Baca bobot per profil dari `calculation_weight`.
  - Daftar & kriteria = **yang tersimpan di snapshot** (bukan yang aktif sekarang).
- **Hidrasi atribut live:** untuk tiap `toy_id` snapshot, ambil atribut tampilan live dari tabel `toys`.
  - Toy dihapus sejak publish → tak ada di `toys` → **dilewati**.
  - Toy/kriteria ditambah sejak publish → tak ada di snapshot → **tak tampil** sampai publish ulang.
    (Inilah gerbang bekerja.)

Dampak per endpoint (semua dari snapshot beku; atribut kartu live):

| Endpoint | Dari snapshot beku | Live |
|---|---|---|
| `top`, `recommend` | skor, ranking, bobot | filter usia/budget + atribut kartu |
| `toys` (+sort per kriteria) | r_ij, skor | nama/harga/stok/filter |
| `toys/{id}` | r_ij bars, skor, rank | atribut mainan |
| `compare` | r_ij, skor per profil | atribut |
| `profiles`, `meta` | bobot beku, CR, daftar kriteria snapshot | daftar kategori (struktur) |

`recommend`: filter usia/budget memakai atribut **live** di atas ranking **beku** (sesuai keputusan).

## 6. Kasus tepi

- **Tidak ada run terpublish** → semua endpoint publik balikan kosong; mobile empty-state
  ("Hasil belum dipublikasikan"). Home & katalog mobile sudah punya `AppEmptyState`.
- **Toy di snapshot sudah dihapus** → dilewati saat hidrasi (tak error).
- **Harga dipakai 2 peran:** input SAW = harga **beku**; Rp yang ditampilkan = **live**. Konsisten
  dengan "skor beku, atribut live".
- **Kriteria non-aktif** tidak muncul di pairwise/kalkulasi/mobile; bobot lamanya diabaikan.

## 7. Pengujian (Definition of Done: `./mvnw test` hijau)

Backend (mirror pola test yang ada, mutating test `@Transactional`):
- `run()` menolak (400) saat precheck gagal (mis. profil CR>0,10) — happy + failure.
- Publish → `top`/`toys` publik memakai skor/ranking dari snapshot, **bukan** edit toy setelah publish
  (buktikan gerbang: ubah rating toy setelah publish → hasil publik tak berubah sampai run+publish ulang).
- Toy baru setelah publish tak muncul di publik sampai publish ulang.
- Tak ada run terpublish → endpoint publik kosong.
- Kriteria non-aktif dikecualikan dari bobot/normalisasi & daftar kriteria publik.
- Regresi: `recommend` profileId, `compare` tetap jalan atas snapshot.

FE/mobile: tak ada perubahan wajib di paket ini (empty-state sudah ada). Perubahan kosmetik ditunda.

## 8. Di luar ruang lingkup (paket berikutnya)

- Sinyal "basi" (#4): flag `stale` di `/dashboard/summary` + banner "publikasikan ulang".
- Mobile: cue "Diperbarui <tgl publish>", empty-state detail/compare, penghalusan bahasa kuis.
- Endpoint unpublish eksplisit.
