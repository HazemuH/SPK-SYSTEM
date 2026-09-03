package com.spkmainan.domain;

import com.spkmainan.category.CategoryEntity;
import com.spkmainan.criterion.CriterionEntity;
import com.spkmainan.criterion.CriterionLevelEntity;
import com.spkmainan.toy.ToyEntity;
import com.spkmainan.weightprofile.WeightProfileEntity;
import java.util.ArrayList;
import java.util.Arrays;
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
        {"kendaraan", "Kendaraan", "Mobil, truk, bus & kendaraan mainan"},
        {"kendaraan-elektronik", "Kendaraan Elektronik", "Kendaraan mainan bermotor & remote"},
        {"kendaraan-anak", "Kendaraan Anak", "Kendaraan yang bisa dinaiki anak"},
        {"elektronik", "Elektronik", "Robot & mainan elektronik"},
        {"boneka", "Boneka", "Boneka & figur karakter"},
        {"role-play", "Role Play", "Mainan bermain peran"},
        {"konstruksi", "Konstruksi", "Balok susun & rakitan"},
        {"edukatif", "Edukatif", "Mainan stimulasi belajar & logika"},
        {"permainan", "Permainan", "Permainan ketangkasan & kartu"},
        {"kreatif", "Kreatif", "Mainan berkarya & eksplorasi"},
        {"fidget", "Fidget", "Mainan sensorik penghilang jenuh"},
        {"olahraga", "Olahraga", "Bola & perlengkapan olahraga anak"},
        {"transportasi", "Transportasi", "Sepeda & skuter anak"},
        {"musik", "Musik", "Alat musik mainan"},
        {"seni", "Seni", "Melukis, mewarnai & kerajinan"},
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

    private record LevelDef(String criterion, String[] labels, double[] priorities) {}

    /**
     * The five subcriteria of every criterion, best band (S1) first, with the AHP
     * local priority each band carries (Tabel 4.7–4.39 of the thesis). Priorities
     * within a criterion sum to 1 — they are what the synthesis multiplies by the
     * criterion weight, so no min/max normalization is involved anywhere.
     */
    private static final LevelDef[] LEVEL_DEF = {
        new LevelDef("keamanan",
            new String[]{"Sangat Aman", "Aman", "Cukup Aman", "Kurang Aman", "Tidak Aman"},
            new double[]{0.416, 0.262, 0.161, 0.099, 0.062}),
        new LevelDef("edukasi",
            new String[]{"Sangat Tinggi", "Tinggi", "Cukup Tinggi", "Rendah", "Sangat Rendah"},
            new double[]{0.522, 0.244, 0.130, 0.061, 0.042}),
        new LevelDef("usia",
            new String[]{"Sangat Sesuai", "Sesuai", "Cukup Sesuai", "Kurang Sesuai", "Tidak Sesuai"},
            new double[]{0.504, 0.259, 0.131, 0.061, 0.045}),
        new LevelDef("harga",
            new String[]{"Sangat Terjangkau (< Rp90.000)", "Terjangkau (Rp90.000 – Rp179.999)",
                "Cukup Terjangkau (Rp180.000 – Rp349.999)", "Mahal (Rp350.000 – Rp599.999)",
                "Sangat Mahal (\u2265 Rp600.000)"},
            new double[]{0.427, 0.274, 0.178, 0.075, 0.045}),
        new LevelDef("kualitas",
            new String[]{"Sangat Baik", "Baik", "Cukup Baik", "Kurang Baik", "Tidak Baik"},
            new double[]{0.456, 0.269, 0.140, 0.082, 0.053}),
        new LevelDef("tahan",
            new String[]{"Sangat Awet", "Awet", "Cukup Awet", "Kurang Awet", "Tidak Awet"},
            new double[]{0.489, 0.235, 0.156, 0.070, 0.050}),
        new LevelDef("material",
            new String[]{"Sangat Baik", "Baik", "Cukup Baik", "Kurang Baik", "Tidak Baik"},
            new double[]{0.447, 0.297, 0.130, 0.080, 0.043}),
        new LevelDef("kreatif",
            new String[]{"Sangat Tinggi", "Tinggi", "Cukup Tinggi", "Rendah", "Sangat Rendah"},
            new double[]{0.502, 0.254, 0.122, 0.080, 0.042}),
        new LevelDef("populer",
            new String[]{"Sangat Populer", "Populer", "Cukup Populer", "Kurang Populer",
                "Tidak Populer"},
            new double[]{0.447, 0.271, 0.160, 0.075, 0.047}),
        new LevelDef("mudah",
            new String[]{"Sangat Mudah", "Mudah", "Cukup Mudah", "Sulit", "Sangat Sulit"},
            new double[]{0.463, 0.261, 0.136, 0.090, 0.050}),
    };

    private record ProfDef(String id, String name, String shortName, String icon, double cr, double lambda,
                           double ci, String desc, boolean isDefault, double[] w) {}

    private static final ProfDef[] PROFILE_DEF = {
        // The main AHP weight vector (Tabel 4.6) — the one the thesis rankings use.
        new ProfDef("balanced", "Seimbang", "Seimbang", "scale", 0.041, 10.55, 0.061,
            "Bobot AHP utama hasil pairwise 10 kriteria — rekomendasi umum.", true,
            new double[]{0.199, 0.180, 0.115, 0.115, 0.115, 0.077, 0.071, 0.054, 0.039, 0.028}),
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
     * Columns: name, category code, price (Rp), age min, age max, tags, and the
     * 1–5 rating per benefit criterion in {@link #CRITERION_DEF} order (Harga is
     * excluded — its band comes from the price).
     */
    private static final Object[][] TOY_DEF = {
        {"Mobil – mobilan", "kendaraan", 120000, 3, 6,
            new String[]{"Tanpa Baterai", "Hemat"}, new int[]{3, 5, 5, 3, 3, 4, 3, 3, 5}},
        {"Mobil Remote Control", "kendaraan-elektronik", 85000, 6, 12,
            new String[]{"Baterai", "Remote"}, new int[]{3, 5, 3, 2, 2, 2, 5, 4, 2}},
        {"Mobil Aki Anak", "kendaraan-anak", 195000, 3, 8,
            new String[]{"Baterai", "Premium"}, new int[]{4, 1, 4, 4, 3, 4, 5, 4, 5}},
        {"Truk Mainan", "kendaraan", 450000, 3, 6,
            new String[]{"Tanpa Baterai", "Outdoor"}, new int[]{4, 3, 5, 4, 4, 5, 2, 4, 5}},
        {"Bus Mainan", "kendaraan", 350000, 3, 6,
            new String[]{"Tanpa Baterai"}, new int[]{5, 5, 3, 4, 5, 4, 5, 4, 2}},
        {"Kereta Mainan", "kendaraan", 275000, 3, 6,
            new String[]{"Baterai", "Bestseller"}, new int[]{4, 1, 3, 4, 3, 4, 4, 4, 3}},
        {"Pesawat Mainan", "kendaraan", 95000, 3, 6,
            new String[]{"Tanpa Baterai"}, new int[]{3, 4, 5, 3, 3, 4, 4, 3, 5}},
        {"Helikopter Mainan", "kendaraan", 75000, 3, 6,
            new String[]{"Baterai"}, new int[]{3, 4, 3, 2, 2, 2, 3, 5, 4}},
        {"Motor Mainan", "kendaraan", 110000, 3, 8,
            new String[]{"Tanpa Baterai"}, new int[]{4, 4, 4, 3, 3, 3, 5, 2, 3}},
        {"Diecast Mobil", "kendaraan", 165000, 5, 12,
            new String[]{"Koleksi", "Hemat"}, new int[]{3, 2, 5, 3, 3, 4, 3, 5, 5}},
        {"Robot Mainan", "elektronik", 520000, 5, 10,
            new String[]{"Baterai", "Roleplay"}, new int[]{5, 4, 3, 4, 5, 4, 3, 5, 3}},
        {"Robot Remote Control", "elektronik", 680000, 6, 12,
            new String[]{"Baterai", "Remote"}, new int[]{5, 4, 4, 5, 5, 5, 5, 2, 3}},
        {"Boneka", "boneka", 135000, 3, 8,
            new String[]{"Lembut", "Hadiah"}, new int[]{3, 4, 5, 3, 3, 4, 4, 3, 5}},
        {"Boneka Karakter", "boneka", 98000, 3, 10,
            new String[]{"Lembut", "Karakter"}, new int[]{3, 3, 2, 3, 3, 3, 3, 5, 2}},
        {"Boneka Bayi", "boneka", 65000, 3, 8,
            new String[]{"Roleplay"}, new int[]{3, 4, 4, 3, 2, 3, 5, 2, 4}},
        {"Boneka Hewan", "boneka", 650000, 3, 8,
            new String[]{"Lembut", "Hadiah"}, new int[]{5, 3, 5, 5, 5, 5, 2, 4, 5}},
        {"Barbie/Doll Set", "boneka", 145000, 5, 12,
            new String[]{"Roleplay", "Bestseller"}, new int[]{3, 5, 3, 3, 3, 3, 5, 4, 2}},
        {"Masak-masakan", "role-play", 88000, 3, 8,
            new String[]{"Roleplay", "Motorik"}, new int[]{3, 1, 4, 3, 2, 3, 5, 4, 5}},
        {"Set Dokter", "role-play", 55000, 3, 8,
            new String[]{"Roleplay", "Edukatif"}, new int[]{2, 2, 4, 2, 2, 3, 2, 5, 4}},
        {"Set Alat Pertukangan", "role-play", 310000, 3, 8,
            new String[]{"Roleplay", "Motorik"}, new int[]{4, 5, 3, 4, 4, 4, 5, 4, 2}},
        {"Balok Susun", "konstruksi", 52000, 3, 8,
            new String[]{"Kayu", "Tanpa Baterai"}, new int[]{3, 3, 4, 3, 2, 3, 5, 2, 4}},
        {"Lego/Brick Building", "konstruksi", 60000, 4, 12,
            new String[]{"Kreatif", "Bestseller"}, new int[]{2, 3, 5, 2, 2, 3, 2, 4, 5}},
        {"Puzzle Anak", "edukatif", 780000, 3, 8,
            new String[]{"Edukatif", "Hemat"}, new int[]{5, 4, 3, 5, 5, 5, 3, 5, 3}},
        {"Puzzle 3D", "edukatif", 70000, 6, 12,
            new String[]{"Kompleks", "Kreatif"}, new int[]{3, 4, 4, 3, 2, 3, 5, 2, 4}},
        {"Nano Block", "konstruksi", 92000, 8, 14,
            new String[]{"Kompleks", "Kreatif"}, new int[]{3, 5, 5, 3, 3, 4, 4, 3, 4}},
        {"Board Game", "edukatif", 240000, 6, 12,
            new String[]{"Keluarga", "Logika"}, new int[]{4, 3, 3, 4, 4, 4, 4, 5, 4}},
        {"Permainan Kartu", "permainan", 335000, 6, 12,
            new String[]{"Keluarga", "Hemat"}, new int[]{4, 1, 3, 4, 3, 4, 4, 4, 3}},
        {"Kartu Edukasi", "edukatif", 420000, 3, 8,
            new String[]{"Edukatif", "Hemat"}, new int[]{4, 3, 5, 4, 4, 5, 2, 4, 5}},
        {"Slime", "kreatif", 78000, 6, 12,
            new String[]{"Sensorik", "Hemat"}, new int[]{3, 5, 3, 2, 2, 2, 5, 4, 3}},
        {"Squishy", "fidget", 115000, 6, 12,
            new String[]{"Sensorik", "Hemat"}, new int[]{4, 2, 4, 3, 3, 3, 4, 3, 5}},
        {"Pop It", "fidget", 230000, 3, 12,
            new String[]{"Sensorik", "Hemat"}, new int[]{4, 5, 5, 3, 4, 4, 3, 3, 5}},
        {"Fidget Toy", "permainan", 295000, 6, 12,
            new String[]{"Sensorik", "Hemat"}, new int[]{4, 5, 3, 4, 4, 4, 5, 4, 2}},
        {"Tembakan Mainan", "permainan", 125000, 6, 12,
            new String[]{"Outdoor", "Motorik"}, new int[]{4, 4, 4, 3, 3, 3, 5, 2, 3}},
        {"Pistol Gelembung", "permainan", 155000, 3, 8,
            new String[]{"Outdoor", "Baterai"}, new int[]{3, 2, 5, 3, 3, 4, 3, 5, 5}},
        {"Bubble Gun", "permainan", 1200000, 3, 8,
            new String[]{"Outdoor", "Baterai"}, new int[]{5, 3, 2, 5, 5, 5, 3, 5, 2}},
        {"Ketapel Mainan", "permainan", 380000, 8, 12,
            new String[]{"Outdoor", "Hemat"}, new int[]{5, 2, 4, 5, 4, 4, 4, 3, 4}},
        {"Layang-layang", "permainan", 105000, 6, 12,
            new String[]{"Outdoor", "Hemat"}, new int[]{3, 4, 5, 3, 3, 4, 4, 3, 5}},
        {"Bola Anak", "olahraga", 88000, 3, 8,
            new String[]{"Outdoor", "Motorik"}, new int[]{3, 4, 3, 2, 2, 2, 3, 5, 4}},
        {"Bola Basket Mini", "olahraga", 140000, 6, 12,
            new String[]{"Indoor", "Motorik"}, new int[]{4, 4, 4, 3, 3, 3, 5, 2, 4}},
        {"Bowling Anak", "olahraga", 365000, 3, 8,
            new String[]{"Indoor", "Motorik"}, new int[]{4, 5, 5, 4, 4, 5, 4, 3, 4}},
        {"Skuter Anak", "transportasi", 45000, 5, 12,
            new String[]{"Outdoor", "Motorik"}, new int[]{3, 5, 3, 2, 2, 2, 5, 4, 2}},
        {"Sepeda Anak", "transportasi", 62000, 4, 12,
            new String[]{"Outdoor", "Motorik"}, new int[]{3, 1, 4, 3, 2, 3, 5, 4, 5}},
        {"Tenda Anak", "role-play", 48000, 3, 10,
            new String[]{"Indoor", "Roleplay"}, new int[]{2, 2, 4, 2, 2, 3, 2, 5, 4}},
        {"Alat Musik Mainan", "musik", 340000, 3, 8,
            new String[]{"Musik", "Sensorik"}, new int[]{4, 4, 3, 4, 4, 4, 3, 5, 3}},
        {"Piano/Keyboard Anak", "musik", 58000, 3, 10,
            new String[]{"Musik", "Baterai"}, new int[]{3, 3, 4, 3, 2, 3, 5, 2, 4}},
        {"Mainan Mewarnai", "seni", 55000, 3, 10,
            new String[]{"Kreatif", "Hemat"}, new int[]{2, 3, 5, 2, 2, 3, 2, 4, 5}},
        {"Set Kerajinan DIY", "seni", 48000, 6, 12,
            new String[]{"Kreatif", "Motorik"}, new int[]{3, 5, 3, 2, 2, 2, 4, 4, 3}},
        {"Mainan Edukasi Berhitung", "edukatif", 175000, 3, 8,
            new String[]{"Edukatif", "Logika"}, new int[]{4, 4, 4, 3, 3, 3, 5, 2, 3}},
        {"Mainan Edukasi Alfabet", "edukatif", 68000, 3, 8,
            new String[]{"Edukatif"}, new int[]{2, 5, 5, 2, 2, 3, 4, 3, 4}},
        {"Celengan DIY/Kerajinan", "edukatif", 185000, 6, 12,
            new String[]{"Kreatif", "Hemat"}, new int[]{4, 3, 3, 4, 4, 4, 4, 5, 4}},
    };

    public static List<CategoryEntity> categories() {
        List<CategoryEntity> out = new ArrayList<>();
        for (String[] c : CATEGORY_DEF) {
            out.add(new CategoryEntity(c[0], c[1], c[2]));
        }
        return out;
    }

    public static List<CriterionLevelEntity> criterionLevels() {
        List<CriterionLevelEntity> out = new ArrayList<>();
        for (LevelDef d : LEVEL_DEF) {
            for (int i = 0; i < d.labels().length; i++) {
                out.add(new CriterionLevelEntity(
                    d.criterion(), i + 1, d.labels()[i], d.priorities()[i]));
            }
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
        List<CritDef> benefits = Arrays.stream(CRITERION_DEF)
            .filter(c -> c.type() == CriterionType.BENEFIT)
            .toList();
        Map<String, String> categoryNames = new LinkedHashMap<>();
        for (String[] c : CATEGORY_DEF) {
            categoryNames.put(c[0], c[1]);
        }

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
            String deskripsi = "Mainan " + categoryNames.getOrDefault(categoryCode, categoryCode)
                + " untuk anak usia " + usiaMin + "–" + usiaMax + " tahun.";
            ToyEntity e = new ToyEntity(name, categoryCode, harga, usiaMin, usiaMax, stok, aktif,
                deskripsi);
            e.setTags(new LinkedHashSet<>(List.of((String[]) t[5])));

            int[] ratings = (int[]) t[6];
            Map<String, Integer> scores = new LinkedHashMap<>();
            for (int j = 0; j < benefits.size(); j++) {
                scores.put(benefits.get(j).code(), ratings[j]);
            }
            e.setScores(scores);
            out.add(e);
        }
        return out;
    }
}
