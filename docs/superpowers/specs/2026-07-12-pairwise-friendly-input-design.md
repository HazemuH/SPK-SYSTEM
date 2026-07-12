# Desain — Input Pairwise Ramah-Awam (slider + kalimat + CR hidup)

**Tanggal:** 2026-07-12 · **Status:** disetujui · **Cakupan:** Frontend saja.

## Masalah
Input pairwise saat ini = 45 dropdown berisi angka `1/7`, `1/5`, dst. Membingungkan (arah
kepentingan tidak intuitif) dan melelahkan. Metode AHP-nya benar; hanya UI-nya yang perlu ramah.

## Keputusan
- **Kontrol per pasangan = slider 9 titik + kalimat hidup.** Posisi tengah = "sama penting";
  geser ke satu nama kriteria = nama itu makin menang. Angka Saaty (`9,7,5,3,1,1/3,…,1/9`) tetap
  dipakai di balik layar (metode tak berubah).
- **Grid matriks tetap** sebagai ringkasan visual (skripsi butuh matriks n×n). Sel `i<j` jadi
  **tombol** (nilai + warna); klik = memilih pasangan untuk diedit di panel bawah. Sel diagonal &
  segitiga bawah tetap otomatis (abu-abu).
- **Badge CR hidup** dihitung di browser tiap kali menggeser (mirror `AhpEngine`).
- **Tombol "Terapkan preset profil"** mengisi ulang matriks dari bobot profil (titik mulai konsisten
  sesuai nama profil).
- **"Hitung Bobot & CR"** tetap → simpan ke backend (CR resmi server harus cocok dengan badge).

## Komponen
- **`src/lib/ahp.ts`** (BARU): `saatyScale` (urut `[1/9,1/7,1/5,1/3,1,3,5,7,9]`), `deriveWeights(matrix,n)`
  → `{weights, lambdaMax, ci, cr, consistent}` — geometric-mean + λmax + CI + CR (RI table
  `[0,0,0,0.58,0.90,1.12,1.24,1.32,1.41,1.45,1.49]`, n≥10 → 1.49), mirror `AhpEngine` persis.
  `intensityLabel(value)`, `comparisonSentence(rowName,colName,value)`.
- **`src/components/ui/slider.tsx`** (BARU): tipis membungkus `<input type="range">` (9 langkah,
  index 0..8), pakai token tema. Aksesibel (label + aria).
- **`src/pages/pairwise/pairwise-page.tsx`** (UBAH): sel grid → tombol pilih; panel editor pasangan
  (slider + kalimat + nama di ujung); badge CR hidup (dari `deriveWeights` atas `matrix` sekarang);
  tombol preset; buang `<select>` per sel. Mapping index↔value lewat `saatyScale`.

## Perilaku
- Nilai sel `matrix["i-j"]` (i<j) tetap = rasio Saaty baris i vs kolom j (kontrak backend tak berubah:
  `PairwiseEntry{rowCode,colCode,value}`).
- Slider index `k` (0..8) → `saatyScale[k]`. Nilai 1 (index 4) = seri. index>4 → baris menang;
  index<4 → kolom menang (nilai < 1).
- Kalimat: `value>1` → "[baris] {intensitas} lebih penting dari [kolom]"; `value<1` → "[kolom] … dari
  [baris]" (pakai 1/value); `=1` → "[baris] & [kolom] sama penting".
- Intensitas: 3="sedikit lebih penting", 5="lebih penting", 7="sangat lebih penting",
  9="mutlak lebih penting".
- CR badge: hijau "Konsisten ✓ (CR x,xx)" bila cr ≤ 0,10; kuning/merah "Belum konsisten (CR x,xx)".

## Uji (DoD: `npm run lint|test|build` hijau)
- `ahp.ts`: matriks konsisten → cr ≈ 0; identitas → weights seragam; sebuah 3-cycle 9 → cr > 0,10;
  `comparisonSentence` untuk value 3 / 1 / (1/3) benar arah & kata.
- `pairwise-page`: render tanpa error; menggeser slider mengubah kalimat + badge CR (component test
  ringan dengan data ter-mock).

## Di luar cakupan
Backend (tak berubah). Preset lanjutan / reduksi kriteria (ditolak — jaga cakupan 10 kriteria).
