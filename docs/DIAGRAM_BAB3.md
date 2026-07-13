# Diagram Bab III — Aplikasi KIDORA (Mermaid)

> Semua diagram di bawah ditulis dalam **Mermaid** dan dapat diekspor menjadi PNG.
> **Cara ekspor ke PNG:**
> 1. Buka <https://mermaid.live> → tempel kode di dalam blok ```mermaid ... ```
>    → menu **Actions → PNG** (atau SVG).
> 2. Atau di **VS Code**: pasang ekstensi *Markdown Preview Mermaid Support* / *Mermaid Editor*,
>    lalu klik kanan diagram → export.
> 3. GitHub merender blok `mermaid` otomatis jadi gambar (bisa di-screenshot).
>
> Catatan: Use Case Diagram bukan tipe native Mermaid, jadi didekati dengan diagram graf
> (aktor — elipsUse case). Kalau butuh notasi UML use case yang persis, gambar ini bisa jadi
> acuan untuk digambar ulang di draw.io/Lucidchart.

---

## Gambar 3.1 — Tahap Penelitian

```mermaid
flowchart TD
    A([Mulai]) --> B[Identifikasi Masalah]
    B --> C[Studi Literatur]
    C --> D[Pengumpulan Data]
    D --> E[Analisis Kebutuhan]
    E --> F[Perancangan Sistem]
    F --> G[Implementasi]
    G --> H[Pengujian]
    H --> I{"Hasil sesuai?"}
    I -->|Tidak| F
    I -->|Ya| J[Kesimpulan dan Saran]
    J --> K([Selesai])
```

---

## Gambar 3.2 — Struktur Informasi Aplikasi KIDORA (User / Mobile)

```mermaid
flowchart TD
    Home["Home / Beranda"]
    Home --> R["Rekomendasi untuk Saya"]
    Home --> K["Jelajah Katalog"]
    Home --> B["Bandingkan Mainan"]

    R --> R1["Kuesioner 1: Usia"]
    R1 --> R2["Kuesioner 2: Budget"]
    R2 --> R3["Kuesioner 3: Tujuan / Kategori"]
    R3 --> R4["Kuesioner 4: Prioritas (pilih profil bobot)"]
    R4 --> R5["Hasil Rekomendasi"]

    K --> K1["Daftar mainan (terurut skor SAW)"]
    K1 --> K2["Detail Mainan"]

    B --> B1["Pilih 2-4 mainan"]
    B1 --> B2["Tabel hasil perbandingan"]
```

---

## Gambar 3.3 — Struktur Informasi Admin (Web)

```mermaid
flowchart TD
    L["Login"] --> D["Dashboard"]
    D --> M1["Data Mainan"]
    D --> M2["Kategori"]
    D --> M3["Kriteria"]
    D --> M4["Profil Bobot"]
    D --> M5["Pairwise"]
    D --> M6["Kalkulasi"]
    D --> M7["Hasil"]
    D --> M8["Laporan"]
    D --> M9["Pengaturan"]

    M1 --> M1a["Tambah / Ubah / Hapus / Aktif-Nonaktif / Export Excel"]
    M2 --> M2a["Tambah / Ubah / Hapus"]
    M3 --> M3a["Tambah / Hapus / Ubah bobot / Aktif-Nonaktif"]
    M4 --> M4a["Tambah / Ubah Pairwise / Lihat detail bobot"]
    M5 --> M5a["Isi slider antar kriteria / Hitung Bobot & CR"]
    M6 --> M6a["Pre-check / Jalankan / Publikasi"]
    M7 --> M7a["Sortir / Cetak / Export Excel"]
    M8 --> M8a["Filter tanggal / Export PDF / Detail"]
    M9 --> M9a["Profil Saya / Keamanan"]
```

---

## Gambar 3.4 — Use Case Diagram (User)

```mermaid
flowchart LR
    U(["User"])
    U --- UC1(("Lihat Rekomendasi"))
    U --- UC2(("Jelajah Katalog"))
    U --- UC3(("Bandingkan Mainan"))
    U --- UC4(("Lihat Detail Mainan"))

    UC1 -. include .-> UC1a(("Isi Kuesioner Preferensi"))
    UC2 -. include .-> UC2a(("Sortir / Filter / Cari"))
    UC3 -. include .-> UC3a(("Pilih 2-4 Mainan"))
```

---

## Gambar 3.5 — Use Case Diagram (Admin)

```mermaid
flowchart LR
    A(["Admin"])
    A --- L(("Login"))
    L -. include .-> U1(("Lihat Dashboard"))
    L -. include .-> U2(("Kelola Data Mainan"))
    L -. include .-> U3(("Kelola Kategori"))
    L -. include .-> U4(("Kelola Kriteria"))
    L -. include .-> U5(("Kelola Profil Bobot"))
    L -. include .-> U6(("Input Pairwise & Hitung CR"))
    L -. include .-> U7(("Jalankan & Publikasi Kalkulasi"))
    L -. include .-> U8(("Lihat Hasil Ranking"))
    L -. include .-> U9(("Lihat & Ekspor Laporan"))
    L -. include .-> U10(("Kelola Pengaturan Akun"))
```

---

## Gambar 3.6 — Activity Diagram User: Rekomendasi untuk Saya

```mermaid
flowchart TD
    S([Mulai]) --> A["Buka aplikasi"]
    A --> B["Pilih menu Rekomendasi untuk Saya"]
    B --> C["Isi kuesioner: usia, budget, tujuan, prioritas"]
    C --> D["Kirim jawaban"]
    D --> E["Sistem menyaring & mengurutkan mainan (AHP-SAW)"]
    E --> F["Menampilkan daftar rekomendasi"]
    F --> G{"Lihat detail?"}
    G -->|Ya| H["Buka Detail Mainan"] --> T([Selesai])
    G -->|Tidak| T
```

---

## Gambar 3.7 — Activity Diagram User: Jelajah Katalog

```mermaid
flowchart TD
    S([Mulai]) --> A["Buka aplikasi"]
    A --> B["Pilih menu Jelajah Katalog"]
    B --> C["Sistem menampilkan daftar mainan terurut skor SAW"]
    C --> D{"Sortir / filter / cari?"}
    D -->|Ya| E["Terapkan sortir/filter/pencarian"] --> C
    D -->|Tidak| F["Pilih mainan"]
    F --> G["Lihat Detail Mainan"]
    G --> T([Selesai])
```

---

## Gambar 3.8 — Activity Diagram User: Bandingkan Mainan

```mermaid
flowchart TD
    S([Mulai]) --> A["Pilih menu Bandingkan Mainan"]
    A --> B["Pilih 2 sampai 4 mainan"]
    B --> C{"Jumlah 2-4?"}
    C -->|Tidak| B
    C -->|Ya| D["Sistem menampilkan tabel perbandingan per kriteria + skor SAW"]
    D --> T([Selesai])
```

---

## Gambar 3.9 & 3.14 — Activity Diagram Admin: Menambahkan Data (Mainan / Kategori)

> Alur ini berlaku untuk **Tambah Mainan** (Gambar 3.9) dan **Tambah Kategori** (Gambar 3.14).

```mermaid
flowchart TD
    S([Mulai]) --> A["Login"]
    A --> B["Pilih menu (Data Mainan / Kategori)"]
    B --> C["Sistem menampilkan daftar"]
    C --> D["Klik + Tambah"]
    D --> E["Sistem menampilkan form / drawer"]
    E --> F["Isi detail data"]
    F --> G["Klik Simpan"]
    G --> H{"Valid?"}
    H -->|Tidak| E
    H -->|Ya| I["Sistem menyimpan data"]
    I --> J["Data tampil di daftar"]
    J --> T([Selesai])
```

---

## Gambar 3.10 & 3.15 — Activity Diagram Admin: Mengubah Data (Mainan / Kategori)

```mermaid
flowchart TD
    S([Mulai]) --> A["Login"]
    A --> B["Buka daftar (Mainan / Kategori)"]
    B --> C["Klik ikon Edit pada baris"]
    C --> D["Sistem menampilkan form berisi data lama"]
    D --> E["Ubah data"]
    E --> F["Klik Simpan"]
    F --> G{"Valid?"}
    G -->|Tidak| D
    G -->|Ya| H["Sistem memperbarui data"]
    H --> I["Perubahan tampil di daftar"]
    I --> T([Selesai])
```

---

## Gambar 3.11 & 3.16 — Activity Diagram Admin: Menghapus Data (Mainan / Kategori)

```mermaid
flowchart TD
    S([Mulai]) --> A["Buka daftar (Mainan / Kategori)"]
    A --> B["Klik ikon Hapus"]
    B --> C["Sistem menampilkan pop-up konfirmasi"]
    C --> D{"Yakin hapus?"}
    D -->|Tidak / Cancel| E["Batal"] --> T([Selesai])
    D -->|Ya / Hapus| F["Sistem menghapus data"]
    F --> G["Data hilang dari daftar"]
    G --> T
```

---

## Gambar 3.12 — Activity Diagram Admin: Aktif / Nonaktif Status Mainan

```mermaid
flowchart TD
    S([Mulai]) --> A["Buka Data Mainan"]
    A --> B["Klik toggle pada kolom Status"]
    B --> C["Sistem memperbarui status"]
    C --> D{"Status aktif?"}
    D -->|Ya| E["Mainan ditampilkan di aplikasi"]
    D -->|Tidak| F["Mainan disembunyikan dari aplikasi"]
    E --> T([Selesai])
    F --> T
```

---

## Gambar 3.13 — Activity Diagram Admin: Export Data ke Excel

```mermaid
flowchart TD
    S([Mulai]) --> A["Buka Data Mainan / Hasil"]
    A --> B["Klik tombol Export Excel"]
    B --> C["Pilih lokasi penyimpanan"]
    C --> D["Sistem membuat berkas Excel"]
    D --> E["Berkas tersimpan di lokasi terpilih"]
    E --> T([Selesai])
```

---

## Gambar 3.17 & 3.18 — Activity Diagram Admin: Input Pairwise / Kuesioner Kriteria

```mermaid
flowchart TD
    S([Mulai]) --> A["Login"]
    A --> B["Pilih menu Pairwise"]
    B --> C["Pilih profil bobot"]
    C --> D["Isi perbandingan antar kriteria (slider Saaty 1-9)"]
    D --> E["Sistem menghitung bobot & CR secara langsung"]
    E --> F["Klik Hitung Bobot & CR"]
    F --> G{"CR ≤ 0,10?"}
    G -->|Tidak| D
    G -->|Ya| H["Sistem menyimpan bobot ke profil"]
    H --> T([Selesai])
```

---

## Gambar 3.19 — Activity Diagram Admin: Kalkulasi & Publikasi AHP-SAW

```mermaid
flowchart TD
    S([Mulai]) --> A["Login"]
    A --> B["Pilih menu Kalkulasi"]
    B --> C["Sistem menampilkan Pre-check"]
    C --> D{"Data valid? (CR ≤ 0,10, rating & harga lengkap)"}
    D -->|Tidak| E["Tombol Jalankan nonaktif — perbaiki data"]
    E --> C
    D -->|Ya| F["Klik Jalankan Kalkulasi"]
    F --> G["Sistem menghitung AHP-SAW & membekukan hasil"]
    G --> H["Menampilkan hasil ranking"]
    H --> I{"Sudah sesuai?"}
    I -->|Tidak| J["Klik Kalkulasi Ulang"] --> C
    I -->|Ya| K["Klik Publikasi"]
    K --> L["Hasil diteruskan ke Laporan & dikonsumsi aplikasi mobile"]
    L --> T([Selesai])
```

---

## Gambar 3.20 — Activity Diagram Admin: Mengubah Profil Saya

```mermaid
flowchart TD
    S([Mulai]) --> A["Login"]
    A --> B["Pilih menu Pengaturan"]
    B --> C["Sistem menampilkan halaman Profil Saya"]
    C --> D["Ubah data profil"]
    D --> E["Klik Simpan Perubahan"]
    E --> F["Sistem memperbarui data profil"]
    F --> T([Selesai])
```

---

## Gambar 3.21 — Activity Diagram Admin: Mengubah Kata Sandi

```mermaid
flowchart TD
    S([Mulai]) --> A["Buka Pengaturan"]
    A --> B["Pilih tab Keamanan"]
    B --> C["Sistem menampilkan form Ubah Password"]
    C --> D["Isi password lama & baru"]
    D --> E["Klik Simpan Perubahan"]
    E --> F{"Valid?"}
    F -->|Tidak| C
    F -->|Ya| G["Sistem memperbarui password"]
    G --> T([Selesai])
```

---

## Gambar 3.22 — Rancangan Basis Data (ERD)

> ERD lengkap 15 tabel. Legenda: `||--o{` = relasi FK (satu-ke-banyak, cascade);
> `||..o{` = tautan lunak lewat kolom `code` (dijaga aplikasi). Rincian kolom ada di `DATABASE.md`.

```mermaid
erDiagram
    users {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar name
        varchar password
        varchar role
    }
    categories {
        bigint id PK
        varchar code UK
        varchar name
        varchar description
    }
    criteria {
        bigint id PK
        varchar code UK
        int no
        varchar name
        varchar type "benefit | cost"
        varchar abbr
        boolean active
    }
    weight_profiles {
        bigint id PK
        varchar code UK
        varchar name
        boolean is_default
        double cr
        double lambda_max
        double ci
    }
    weight_profile_weights {
        bigint weight_profile_id PK,FK
        varchar criterion_code PK
        double weight
    }
    toys {
        bigint id PK
        varchar name
        varchar category_code
        bigint price
        int age_min
        int age_max
        int stock
        boolean active
    }
    toy_tags {
        bigint toy_id FK
        varchar tag
    }
    toy_scores {
        bigint toy_id PK,FK
        varchar criterion_code PK
        int rating "1..5"
    }
    calculation_runs {
        bigint id PK
        varchar code UK
        timestamp run_at
        int alt_count
        boolean published
        timestamp published_at
    }
    calculation_results {
        bigint id PK
        bigint run_id FK
        varchar profile_code
        double cr
        boolean consistent
        int best_toy_id
    }
    ranking_entries {
        bigint id PK
        bigint result_id FK
        int toy_id
        int rank_no
        double saw_score
    }
    calculation_criteria {
        bigint id PK
        bigint run_id FK
        varchar criterion_code
        varchar type
    }
    calculation_weights {
        bigint id PK
        bigint result_id FK
        varchar criterion_code
        double weight
    }
    calculation_norms {
        bigint id PK
        bigint run_id FK
        int toy_id
        varchar criterion_code
        double norm_value
    }

    weight_profiles ||--o{ weight_profile_weights : "punya"
    toys ||--o{ toy_tags : "punya"
    toys ||--o{ toy_scores : "punya"
    calculation_runs ||--o{ calculation_results : "punya"
    calculation_results ||--o{ ranking_entries : "punya"
    calculation_runs ||--o{ calculation_criteria : "membekukan"
    calculation_results ||--o{ calculation_weights : "membekukan"
    calculation_runs ||--o{ calculation_norms : "membekukan"
    categories ||..o{ toys : "via code"
    criteria ||..o{ toy_scores : "via code"
    criteria ||..o{ weight_profile_weights : "via code"
    weight_profiles ||..o{ calculation_results : "via code"
```

---

# Diagram Tambahan (disarankan untuk memperkuat Bab III)

## T-1 — Arsitektur Sistem

```mermaid
flowchart LR
    subgraph Klien
      MOB["Aplikasi Mobile<br/>(Flutter) — tanpa login"]
      WEB["Web Admin<br/>(React) — dengan login"]
    end
    MOB -->|"REST /v1/public"| BE
    WEB -->|"REST /v1 + JWT"| BE
    BE["Backend (Spring Boot)<br/>Mesin AHP-SAW"] --> DB[("PostgreSQL")]
```

## T-2 — Flowchart Algoritma AHP-SAW

```mermaid
flowchart TD
    S([Mulai]) --> A["Input matriks perbandingan berpasangan antar kriteria (Saaty 1-9)"]
    A --> B["Hitung bobot: rata-rata geometris lalu normalisasi"]
    B --> C["Hitung lambda-max, CI, dan CR"]
    C --> D{"CR ≤ 0,10?"}
    D -->|Tidak| A
    D -->|Ya| E["Susun matriks keputusan: rating 1-5 + harga"]
    E --> F["Normalisasi: benefit rij = xij/max ; cost rij = min/xij"]
    F --> G["Hitung skor SAW: Si = jumlah (wj * rij)"]
    G --> H["Urutkan menurun berdasarkan Si -> ranking"]
    H --> T([Selesai])
```

## T-3 — Sequence Diagram: Kalkulasi & Publikasi (Admin)

```mermaid
sequenceDiagram
    actor Admin
    participant Web as Web Admin
    participant API as Backend (AHP-SAW)
    participant DB as PostgreSQL
    Admin->>Web: Buka menu Kalkulasi
    Web->>API: GET pre-check
    API->>DB: Ambil kriteria, bobot, mainan
    DB-->>API: Data
    API-->>Web: Status valid / tidak
    Admin->>Web: Jalankan Kalkulasi
    Web->>API: POST /calculations/run
    API->>API: Normalisasi + skor SAW
    API->>DB: Simpan hasil + snapshot beku
    Admin->>Web: Publikasi
    Web->>API: POST /calculations/{id}/publish
    API->>DB: Set published = true
    API-->>Web: Berhasil
```

## T-4 — Sequence Diagram: Rekomendasi (User / Mobile)

```mermaid
sequenceDiagram
    actor User
    participant App as Aplikasi Mobile
    participant API as Backend
    participant DB as PostgreSQL
    User->>App: Isi kuesioner (usia, budget, tujuan, prioritas)
    App->>API: POST /public/recommend
    API->>DB: Ambil snapshot terpublish (bobot + skor beku)
    DB-->>API: Data
    API->>API: Filter usia/budget + urutkan skor SAW
    API-->>App: Daftar rekomendasi
    App-->>User: Menampilkan hasil rekomendasi
```
