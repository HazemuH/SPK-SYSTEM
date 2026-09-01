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
 * 5 weight profiles, and the 50 alternatives A1–A50 (with 1–5 benefit ratings
 * computed the same way).
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

    /**
     * The 50 alternatives (A1–A50) in thesis order — array index i maps to code
     * "A" + (i + 1), which is also the insertion order used by {@code DomainSeeder}.
     * Columns: name, category code, price (Rp), age min, age max, tags.
     */
    private static final Object[][] TOY_DEF = {
        {"Mobil – mobilan", "kendaraan", 45000, 2, 6, new String[]{"Tanpa Baterai", "Hemat"}},
        {"Mobil Remote Control", "kendaraan", 275000, 5, 12, new String[]{"Baterai", "Remote"}},
        {"Mobil Aki Anak", "kendaraan", 1250000, 2, 6, new String[]{"Baterai", "Premium"}},
        {"Truk Mainan", "kendaraan", 95000, 2, 7, new String[]{"Tanpa Baterai", "Outdoor"}},
        {"Bus Mainan", "kendaraan", 85000, 2, 7, new String[]{"Tanpa Baterai"}},
        {"Kereta Mainan", "kendaraan", 210000, 3, 8, new String[]{"Baterai", "Bestseller"}},
        {"Pesawat Mainan", "kendaraan", 70000, 3, 9, new String[]{"Tanpa Baterai"}},
        {"Helikopter Mainan", "kendaraan", 180000, 5, 12, new String[]{"Baterai"}},
        {"Motor Mainan", "kendaraan", 90000, 3, 8, new String[]{"Tanpa Baterai"}},
        {"Diecast Mobil", "kendaraan", 55000, 4, 12, new String[]{"Koleksi", "Hemat"}},
        {"Robot Mainan", "boneka", 150000, 4, 10, new String[]{"Baterai", "Roleplay"}},
        {"Robot Remote Control", "boneka", 320000, 6, 12, new String[]{"Baterai", "Remote"}},
        {"Boneka", "boneka", 95000, 1, 8, new String[]{"Lembut", "Hadiah"}},
        {"Boneka Karakter", "boneka", 145000, 3, 10, new String[]{"Lembut", "Karakter"}},
        {"Boneka Bayi", "boneka", 165000, 3, 9, new String[]{"Roleplay"}},
        {"Boneka Hewan", "boneka", 110000, 1, 8, new String[]{"Lembut", "Hadiah"}},
        {"Barbie/Doll Set", "boneka", 235000, 4, 12, new String[]{"Roleplay", "Bestseller"}},
        {"Masak-masakan", "boneka", 175000, 3, 8, new String[]{"Roleplay", "Motorik"}},
        {"Set Dokter", "boneka", 120000, 3, 8, new String[]{"Roleplay", "Edukatif"}},
        {"Set Alat Pertukangan", "boneka", 135000, 3, 9, new String[]{"Roleplay", "Motorik"}},
        {"Balok Susun", "konstruksi", 130000, 2, 6, new String[]{"Kayu", "Tanpa Baterai"}},
        {"Lego/Brick Building", "konstruksi", 360000, 5, 12, new String[]{"Kreatif", "Bestseller"}},
        {"Puzzle Anak", "puzzle", 65000, 3, 8, new String[]{"Edukatif", "Hemat"}},
        {"Puzzle 3D", "puzzle", 145000, 7, 14, new String[]{"Kompleks", "Kreatif"}},
        {"Nano Block", "konstruksi", 115000, 8, 15, new String[]{"Kompleks", "Kreatif"}},
        {"Board Game", "puzzle", 195000, 6, 14, new String[]{"Keluarga", "Logika"}},
        {"Permainan Kartu", "puzzle", 45000, 5, 12, new String[]{"Keluarga", "Hemat"}},
        {"Kartu Edukasi", "edukatif", 60000, 2, 7, new String[]{"Edukatif", "Hemat"}},
        {"Slime", "seni", 35000, 5, 12, new String[]{"Sensorik", "Hemat"}},
        {"Squishy", "seni", 30000, 4, 10, new String[]{"Sensorik", "Hemat"}},
        {"Pop It", "seni", 28000, 3, 10, new String[]{"Sensorik", "Hemat"}},
        {"Fidget Toy", "seni", 40000, 5, 12, new String[]{"Sensorik", "Hemat"}},
        {"Tembakan Mainan", "outdoor", 85000, 6, 12, new String[]{"Outdoor", "Motorik"}},
        {"Pistol Gelembung", "outdoor", 65000, 3, 8, new String[]{"Outdoor", "Baterai"}},
        {"Bubble Gun", "outdoor", 75000, 3, 8, new String[]{"Outdoor", "Baterai"}},
        {"Ketapel Mainan", "outdoor", 35000, 7, 12, new String[]{"Outdoor", "Hemat"}},
        {"Layang-layang", "outdoor", 40000, 5, 12, new String[]{"Outdoor", "Hemat"}},
        {"Bola Anak", "olahraga", 55000, 2, 10, new String[]{"Outdoor", "Motorik"}},
        {"Bola Basket Mini", "olahraga", 145000, 4, 10, new String[]{"Indoor", "Motorik"}},
        {"Bowling Anak", "olahraga", 95000, 3, 9, new String[]{"Indoor", "Motorik"}},
        {"Skuter Anak", "outdoor", 480000, 4, 10, new String[]{"Outdoor", "Motorik"}},
        {"Sepeda Anak", "outdoor", 850000, 3, 10, new String[]{"Outdoor", "Motorik"}},
        {"Tenda Anak", "outdoor", 320000, 2, 8, new String[]{"Indoor", "Roleplay"}},
        {"Alat Musik Mainan", "seni", 125000, 2, 7, new String[]{"Musik", "Sensorik"}},
        {"Piano/Keyboard Anak", "seni", 285000, 3, 9, new String[]{"Musik", "Baterai"}},
        {"Mainan Mewarnai", "seni", 55000, 3, 9, new String[]{"Kreatif", "Hemat"}},
        {"Set Kerajinan DIY", "seni", 110000, 6, 12, new String[]{"Kreatif", "Motorik"}},
        {"Mainan Edukasi Berhitung", "edukatif", 80000, 4, 9, new String[]{"Edukatif", "Logika"}},
        {"Mainan Edukasi Alfabet", "edukatif", 75000, 3, 8, new String[]{"Edukatif"}},
        {"Celengan DIY/Kerajinan", "seni", 65000, 5, 11, new String[]{"Kreatif", "Hemat"}},
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
            int stok = 6 + ((i * 13) % 40);
            // Every alternative A1–A50 takes part in the ranking.
            boolean aktif = true;
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
