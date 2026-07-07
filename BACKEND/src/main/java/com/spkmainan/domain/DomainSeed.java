package com.spkmainan.domain;

import com.spkmainan.category.CategoryEntity;
import com.spkmainan.criterion.CriterionEntity;
import com.spkmainan.toy.ToyEntity;
import com.spkmainan.weightprofile.WeightProfileEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Static seed definitions ported verbatim from the Hi-Fi design
 * (design/ahp-toko-mainan/project/hifi/data.jsx): 8 categories, 10 criteria,
 * 5 weight profiles, 50 toys (with 1–5 benefit ratings computed the same way).
 * Builds JPA entities for {@code DomainSeeder} to persist on first startup.
 */
public final class DomainSeed {

    private DomainSeed() {
    }

    private static final String[][] CATEGORY_DEF = {
        {"edukatif", "Edukatif", "Mainan stimulasi belajar & logika"},
        {"outdoor", "Outdoor", "Aktivitas luar ruang & gerak"},
        {"puzzle", "Puzzle", "Teka-teki & pemecahan masalah"},
        {"boneka", "Boneka", "Boneka & permainan peran"},
        {"kendaraan", "Kendaraan", "Mobil, kereta & kendaraan mainan"},
        {"konstruksi", "Konstruksi", "Balok susun & rakitan"},
        {"seni", "Seni & Kreativitas", "Melukis, mewarnai & berkarya"},
        {"olahraga", "Olahraga", "Bola & perlengkapan olahraga anak"},
    };

    private record CritDef(String code, int no, String name, CriterionType type, String desc, String abbr) {}

    private static final CritDef[] CRITERION_DEF = {
        new CritDef("keamanan", 1, "Keamanan", CriterionType.BENEFIT, "Keamanan desain & penggunaan untuk anak", "Aman"),
        new CritDef("edukasi", 2, "Nilai Edukasi", CriterionType.BENEFIT, "Manfaat pembelajaran & stimulasi", "Edukasi"),
        new CritDef("usia", 3, "Kesesuaian Usia", CriterionType.BENEFIT, "Kecocokan dengan tahap perkembangan", "Usia"),
        new CritDef("harga", 4, "Harga", CriterionType.COST, "Harga jual — makin murah makin baik", "Harga"),
        new CritDef("kualitas", 5, "Kualitas Produk", CriterionType.BENEFIT, "Mutu pembuatan & finishing", "Kualitas"),
        new CritDef("tahan", 6, "Daya Tahan", CriterionType.BENEFIT, "Ketahanan terhadap pemakaian", "Tahan"),
        new CritDef("material", 7, "Material", CriterionType.BENEFIT, "Mutu & keamanan bahan baku", "Material"),
        new CritDef("kreatif", 8, "Kreativitas", CriterionType.BENEFIT, "Stimulasi imajinasi & kreativitas", "Kreatif"),
        new CritDef("populer", 9, "Popularitas", CriterionType.BENEFIT, "Rating & permintaan pasar", "Populer"),
        new CritDef("mudah", 10, "Kemudahan Penggunaan", CriterionType.BENEFIT, "Mudah dimainkan anak", "Mudah"),
    };

    private record ProfDef(String id, String name, String shortName, String icon, double cr, double lambda,
                           double ci, String desc, boolean isDefault, double[] w) {}

    private static final ProfDef[] PROFILE_DEF = {
        new ProfDef("balanced", "Seimbang", "Seimbang", "scale", 0.041, 10.55, 0.061,
            "Bobot proporsional untuk semua kriteria — rekomendasi umum.", true,
            new double[]{0.20, 0.16, 0.13, 0.12, 0.11, 0.09, 0.07, 0.05, 0.04, 0.03}),
        new ProfDef("safety", "Utamakan Keamanan", "Keamanan", "lock", 0.058, 10.78, 0.086,
            "Menonjolkan keamanan & material — ideal untuk balita.", false,
            new double[]{0.34, 0.12, 0.12, 0.07, 0.10, 0.08, 0.10, 0.03, 0.02, 0.02}),
        new ProfDef("education", "Utamakan Edukasi", "Edukasi", "doc", 0.052, 10.70, 0.078,
            "Memprioritaskan nilai edukatif & kreativitas.", false,
            new double[]{0.16, 0.32, 0.13, 0.07, 0.08, 0.05, 0.05, 0.10, 0.02, 0.02}),
        new ProfDef("price", "Utamakan Harga", "Harga", "tag", 0.063, 10.84, 0.094,
            "Mengejar value terbaik — bobot harga dominan.", false,
            new double[]{0.16, 0.10, 0.10, 0.34, 0.08, 0.07, 0.05, 0.04, 0.03, 0.03}),
        new ProfDef("durability", "Utamakan Keawetan", "Keawetan", "shield", 0.049, 10.66, 0.073,
            "Fokus daya tahan, kualitas & material.", false,
            new double[]{0.15, 0.10, 0.09, 0.08, 0.18, 0.20, 0.12, 0.03, 0.03, 0.02}),
    };

    private static final Object[][] TOY_DEF = {
        {"Balok Kayu Edukasi Pelangi", "edukatif", 120000, 2, 6, new String[]{"Kayu", "Tanpa Baterai", "Bestseller"}},
        {"Puzzle Hewan Nusantara 100pcs", "puzzle", 85000, 4, 8, new String[]{"Kayu", "Edukatif"}},
        {"Boneka Barbie Fashionista", "boneka", 195000, 3, 10, new String[]{"Roleplay"}},
        {"Sepeda Roda Tiga Anak", "outdoor", 450000, 2, 5, new String[]{"Outdoor", "Motorik"}},
        {"Lego Classic Creative Bricks", "konstruksi", 350000, 6, 12, new String[]{"Bestseller", "Kreatif"}},
        {"Mobil Remote Control Off-Road", "kendaraan", 275000, 6, 14, new String[]{"Baterai", "Remote"}},
        {"Play-Doh Set Kreasi Warna", "seni", 95000, 3, 8, new String[]{"Kreatif", "Sensorik"}},
        {"Bola Sepak Mini Anak", "olahraga", 75000, 4, 10, new String[]{"Outdoor", "Motorik"}},
        {"Puzzle Peta Indonesia Kayu", "puzzle", 110000, 5, 10, new String[]{"Kayu", "Edukatif"}},
        {"Boneka Beruang Teddy Jumbo", "boneka", 165000, 1, 8, new String[]{"Lembut", "Hadiah"}},
        {"Skuter Lipat Roda Tiga", "outdoor", 520000, 3, 8, new String[]{"Outdoor", "Motorik"}},
        {"Lego Technic Excavator", "konstruksi", 680000, 9, 16, new String[]{"Kompleks", "Kreatif"}},
        {"Meja Lukis Anak Magnetik", "seni", 135000, 3, 9, new String[]{"Kreatif", "Reusable"}},
        {"Truk Pasir Pantai Besar", "kendaraan", 98000, 2, 6, new String[]{"Outdoor", "Tanpa Baterai"}},
        {"Abacus Sempoa Edukasi", "edukatif", 65000, 4, 10, new String[]{"Kayu", "Edukatif", "Hemat"}},
        {"Trampolin Mini Indoor", "olahraga", 650000, 3, 8, new String[]{"Indoor", "Motorik"}},
        {"Puzzle 3D Menara Eiffel", "puzzle", 145000, 8, 15, new String[]{"Kompleks", "Kreatif"}},
        {"Boneka Tangan Hewan Set", "boneka", 88000, 2, 7, new String[]{"Roleplay", "Lembut"}},
        {"Mobil-mobilan Pull Back", "kendaraan", 55000, 3, 8, new String[]{"Tanpa Baterai", "Hemat"}},
        {"Kereta Api Kayu Set Rel", "konstruksi", 310000, 3, 9, new String[]{"Kayu", "Bestseller"}},
        {"Crayon Jumbo 24 Warna", "seni", 52000, 2, 8, new String[]{"Kreatif", "Hemat"}},
        {"Hula Hoop Pelangi Anak", "olahraga", 60000, 4, 12, new String[]{"Outdoor", "Hemat"}},
        {"Sepeda BMX Anak 16 inch", "outdoor", 780000, 5, 10, new String[]{"Outdoor", "Motorik"}},
        {"Flashcard Alfabet & Angka", "edukatif", 70000, 2, 6, new String[]{"Edukatif", "Hemat"}},
        {"Puzzle Lantai Busa Angka", "puzzle", 92000, 1, 5, new String[]{"Sensorik", "Edukatif"}},
        {"Boneka Bayi Reborn", "boneka", 240000, 4, 10, new String[]{"Roleplay"}},
        {"Helikopter Remote Mini", "kendaraan", 335000, 8, 15, new String[]{"Baterai", "Remote"}},
        {"Tenda Bermain Istana", "outdoor", 420000, 2, 7, new String[]{"Indoor", "Roleplay"}},
        {"Slime Kit DIY Glitter", "seni", 78000, 5, 12, new String[]{"Sensorik", "Kreatif"}},
        {"Raket Badminton Anak Set", "olahraga", 115000, 5, 12, new String[]{"Outdoor", "Motorik"}},
        {"Mikroskop Mainan Edukasi", "edukatif", 230000, 7, 14, new String[]{"Edukatif", "Sains"}},
        {"Lego Duplo Kebun Binatang", "konstruksi", 295000, 2, 5, new String[]{"Bestseller", "Balita"}},
        {"Puzzle Jigsaw Dinosaurus 200pcs", "puzzle", 125000, 6, 12, new String[]{"Kompleks"}},
        {"Boneka Action Figure Superhero", "boneka", 155000, 4, 12, new String[]{"Roleplay"}},
        {"Mobil Aki Anak Jeep", "kendaraan", 1200000, 2, 6, new String[]{"Baterai", "Premium"}},
        {"Ayunan Indoor Anak", "outdoor", 380000, 2, 7, new String[]{"Indoor", "Motorik"}},
        {"Kanvas Lukis & Cat Air Set", "seni", 105000, 5, 13, new String[]{"Kreatif"}},
        {"Set Bowling Mini Anak", "olahraga", 88000, 3, 9, new String[]{"Indoor", "Motorik"}},
        {"Globe Dunia Berputar Edukasi", "edukatif", 140000, 6, 14, new String[]{"Edukatif", "Sains"}},
        {"Magnetic Tiles 60pcs", "konstruksi", 365000, 3, 10, new String[]{"Kreatif", "Bestseller"}},
        {"Puzzle Kubus Rubik 3x3", "puzzle", 45000, 7, 15, new String[]{"Hemat", "Logika"}},
        {"Boneka Jari Cerita Dongeng", "boneka", 62000, 2, 6, new String[]{"Roleplay", "Hemat"}},
        {"Pesawat Terbang Styrofoam", "kendaraan", 48000, 5, 12, new String[]{"Outdoor", "Hemat"}},
        {"Kolam Bola Anak Portable", "outdoor", 340000, 1, 5, new String[]{"Indoor", "Sensorik"}},
        {"Plastisin Lilin Malam 12 Warna", "seni", 58000, 3, 9, new String[]{"Sensorik", "Hemat"}},
        {"Skipping Rope Penghitung", "olahraga", 55000, 6, 14, new String[]{"Outdoor", "Hemat"}},
        {"Buku Aktivitas Stiker Edukasi", "edukatif", 48000, 3, 7, new String[]{"Edukatif", "Hemat"}},
        {"Set Konstruksi Mur & Baut", "konstruksi", 175000, 4, 10, new String[]{"Motorik", "Kreatif"}},
        {"Puzzle Tangram Kayu Klasik", "puzzle", 68000, 5, 12, new String[]{"Kayu", "Logika"}},
        {"Boneka Masak-masakan Set Dapur", "boneka", 185000, 3, 8, new String[]{"Roleplay"}},
    };

    // CAT_BASE keyed by category code: [edukasi, usia, kreatif, populer, mudah]
    private static final Map<String, int[]> CAT_BASE = Map.of(
        "edukatif", new int[]{5, 4, 4, 3, 4},
        "outdoor", new int[]{3, 4, 3, 4, 4},
        "puzzle", new int[]{5, 4, 5, 3, 3},
        "boneka", new int[]{2, 4, 4, 5, 5},
        "kendaraan", new int[]{2, 3, 3, 5, 3},
        "konstruksi", new int[]{5, 4, 5, 3, 3},
        "seni", new int[]{4, 4, 5, 3, 4},
        "olahraga", new int[]{3, 4, 3, 4, 5});

    public static List<CategoryEntity> categories() {
        List<CategoryEntity> out = new ArrayList<>();
        for (String[] c : CATEGORY_DEF) {
            out.add(new CategoryEntity(c[0], c[1], c[2]));
        }
        return out;
    }

    public static List<CriterionEntity> criteria() {
        List<CriterionEntity> out = new ArrayList<>();
        for (CritDef c : CRITERION_DEF) {
            out.add(new CriterionEntity(c.code(), c.no(), c.name(), c.type(), c.desc(), c.abbr(), true));
        }
        return out;
    }

    public static List<WeightProfileEntity> weightProfiles() {
        List<WeightProfileEntity> out = new ArrayList<>();
        for (ProfDef p : PROFILE_DEF) {
            WeightProfileEntity e = new WeightProfileEntity(
                p.id(), p.name(), p.shortName(), p.icon(), p.desc(), p.isDefault(), true);
            e.setCr(p.cr());
            e.setLambdaMax(p.lambda());
            e.setCi(p.ci());
            Map<String, Double> w = new LinkedHashMap<>();
            for (int i = 0; i < CRITERION_DEF.length; i++) {
                w.put(CRITERION_DEF[i].code(), p.w()[i]);
            }
            e.setWeights(w);
            out.add(e);
        }
        return out;
    }

    public static List<ToyEntity> toys() {
        List<ToyEntity> out = new ArrayList<>();
        for (int i = 0; i < TOY_DEF.length; i++) {
            Object[] t = TOY_DEF[i];
            String name = (String) t[0];
            String categoryCode = (String) t[1];
            long harga = ((Number) t[2]).longValue();
            int usiaMin = (int) t[3];
            int usiaMax = (int) t[4];
            int stok = (i % 11 == 3) ? 0 : 4 + ((i * 13) % 40);
            boolean aktif = (i % 17) != 9;
            String deskripsi = "Mainan " + categoryCode + " untuk anak usia " + usiaMin + "–" + usiaMax + " tahun.";
            ToyEntity e = new ToyEntity(name, categoryCode, harga, usiaMin, usiaMax, stok, aktif, deskripsi);
            e.setTags(new LinkedHashSet<>(List.of((String[]) t[5])));
            e.setScores(scoreFor(categoryCode, harga, i));
            out.add(e);
        }
        return out;
    }

    // ── scoring ported from data.jsx scoreFor() ──────────────────────────
    private static int clamp5(double v) {
        return (int) Math.max(1, Math.min(5, Math.round(v)));
    }

    private static int priceTier(long h) {
        if (h >= 600000) return 5;
        if (h >= 350000) return 4;
        if (h >= 180000) return 3;
        if (h >= 90000) return 2;
        return 1;
    }

    private static int jit(int i, int k) {
        return ((i * 7 + k * 13) % 3) - 1;
    }

    private static Map<String, Integer> scoreFor(String categoryCode, long harga, int i) {
        int[] b = CAT_BASE.getOrDefault(categoryCode, CAT_BASE.get("edukatif"));
        int tier = priceTier(harga);
        Map<String, Integer> s = new LinkedHashMap<>();
        s.put("keamanan", clamp5(2.3 + tier * 0.55 + jit(i, 0) * 0.4));
        s.put("edukasi", clamp5(b[0] + jit(i, 1)));
        s.put("usia", clamp5(b[1] + jit(i, 2)));
        s.put("kualitas", clamp5(1.5 + tier * 0.7 + jit(i, 3) * 0.3));
        s.put("tahan", clamp5(1.4 + tier * 0.72 + jit(i, 4) * 0.3));
        s.put("material", clamp5(2.0 + tier * 0.6 + jit(i, 5) * 0.3));
        s.put("kreatif", clamp5(b[2] + jit(i, 6)));
        s.put("populer", clamp5(b[3] + jit(i, 7)));
        s.put("mudah", clamp5(b[4] + jit(i, 8)));
        return s;
    }
}
