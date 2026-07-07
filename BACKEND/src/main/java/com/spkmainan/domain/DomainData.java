package com.spkmainan.domain;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * In-memory seed of the SPK domain, ported verbatim from the Hi-Fi design
 * (design/ahp-toko-mainan/project/hifi/data.jsx): 8 categories, 10 criteria,
 * 5 weight profiles, 50 toys (with 1–5 benefit ratings computed the same way).
 *
 * <p>Phase 1 keeps this in memory so the public read API works end-to-end;
 * a later phase moves it behind JPA + admin CRUD without changing the engine.
 */
@Component
public class DomainData {

    // ── Categories (id, name, description) ──────────────────────────────
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

    // ── Criteria: 9 benefit + 1 cost (harga) ────────────────────────────
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

    // ── Weight profiles: id, name, short, icon, cr, lambda, ci, desc, weights ──
    private record ProfDef(String id, String name, String shortName, String icon, double cr, double lambda,
                           double ci, String desc, double[] w) {}

    // weight order aligns with CRITERION_DEF order
    private static final ProfDef[] PROFILE_DEF = {
        new ProfDef("balanced", "Seimbang", "Seimbang", "scale", 0.041, 10.55, 0.061,
            "Bobot proporsional untuk semua kriteria — rekomendasi umum.",
            new double[]{0.20, 0.16, 0.13, 0.12, 0.11, 0.09, 0.07, 0.05, 0.04, 0.03}),
        new ProfDef("safety", "Utamakan Keamanan", "Keamanan", "lock", 0.058, 10.78, 0.086,
            "Menonjolkan keamanan & material — ideal untuk balita.",
            new double[]{0.34, 0.12, 0.12, 0.07, 0.10, 0.08, 0.10, 0.03, 0.02, 0.02}),
        new ProfDef("education", "Utamakan Edukasi", "Edukasi", "doc", 0.052, 10.70, 0.078,
            "Memprioritaskan nilai edukatif & kreativitas.",
            new double[]{0.16, 0.32, 0.13, 0.07, 0.08, 0.05, 0.05, 0.10, 0.02, 0.02}),
        new ProfDef("price", "Utamakan Harga", "Harga", "tag", 0.063, 10.84, 0.094,
            "Mengejar value terbaik — bobot harga dominan.",
            new double[]{0.16, 0.10, 0.10, 0.34, 0.08, 0.07, 0.05, 0.04, 0.03, 0.03}),
        new ProfDef("durability", "Utamakan Keawetan", "Keawetan", "shield", 0.049, 10.66, 0.073,
            "Fokus daya tahan, kualitas & material.",
            new double[]{0.15, 0.10, 0.09, 0.08, 0.18, 0.20, 0.12, 0.03, 0.03, 0.02}),
    };

    // ── 50 toys: name, category, price, ageMin, ageMax, tags ─────────────
    private static final Object[][] TOY_DEF = {
        {"Balok Kayu Edukasi Pelangi", "Edukatif", 120000, 2, 6, new String[]{"Kayu", "Tanpa Baterai", "Bestseller"}},
        {"Puzzle Hewan Nusantara 100pcs", "Puzzle", 85000, 4, 8, new String[]{"Kayu", "Edukatif"}},
        {"Boneka Barbie Fashionista", "Boneka", 195000, 3, 10, new String[]{"Roleplay"}},
        {"Sepeda Roda Tiga Anak", "Outdoor", 450000, 2, 5, new String[]{"Outdoor", "Motorik"}},
        {"Lego Classic Creative Bricks", "Konstruksi", 350000, 6, 12, new String[]{"Bestseller", "Kreatif"}},
        {"Mobil Remote Control Off-Road", "Kendaraan", 275000, 6, 14, new String[]{"Baterai", "Remote"}},
        {"Play-Doh Set Kreasi Warna", "Seni & Kreativitas", 95000, 3, 8, new String[]{"Kreatif", "Sensorik"}},
        {"Bola Sepak Mini Anak", "Olahraga", 75000, 4, 10, new String[]{"Outdoor", "Motorik"}},
        {"Puzzle Peta Indonesia Kayu", "Puzzle", 110000, 5, 10, new String[]{"Kayu", "Edukatif"}},
        {"Boneka Beruang Teddy Jumbo", "Boneka", 165000, 1, 8, new String[]{"Lembut", "Hadiah"}},
        {"Skuter Lipat Roda Tiga", "Outdoor", 520000, 3, 8, new String[]{"Outdoor", "Motorik"}},
        {"Lego Technic Excavator", "Konstruksi", 680000, 9, 16, new String[]{"Kompleks", "Kreatif"}},
        {"Meja Lukis Anak Magnetik", "Seni & Kreativitas", 135000, 3, 9, new String[]{"Kreatif", "Reusable"}},
        {"Truk Pasir Pantai Besar", "Kendaraan", 98000, 2, 6, new String[]{"Outdoor", "Tanpa Baterai"}},
        {"Abacus Sempoa Edukasi", "Edukatif", 65000, 4, 10, new String[]{"Kayu", "Edukatif", "Hemat"}},
        {"Trampolin Mini Indoor", "Olahraga", 650000, 3, 8, new String[]{"Indoor", "Motorik"}},
        {"Puzzle 3D Menara Eiffel", "Puzzle", 145000, 8, 15, new String[]{"Kompleks", "Kreatif"}},
        {"Boneka Tangan Hewan Set", "Boneka", 88000, 2, 7, new String[]{"Roleplay", "Lembut"}},
        {"Mobil-mobilan Pull Back", "Kendaraan", 55000, 3, 8, new String[]{"Tanpa Baterai", "Hemat"}},
        {"Kereta Api Kayu Set Rel", "Konstruksi", 310000, 3, 9, new String[]{"Kayu", "Bestseller"}},
        {"Crayon Jumbo 24 Warna", "Seni & Kreativitas", 52000, 2, 8, new String[]{"Kreatif", "Hemat"}},
        {"Hula Hoop Pelangi Anak", "Olahraga", 60000, 4, 12, new String[]{"Outdoor", "Hemat"}},
        {"Sepeda BMX Anak 16 inch", "Outdoor", 780000, 5, 10, new String[]{"Outdoor", "Motorik"}},
        {"Flashcard Alfabet & Angka", "Edukatif", 70000, 2, 6, new String[]{"Edukatif", "Hemat"}},
        {"Puzzle Lantai Busa Angka", "Puzzle", 92000, 1, 5, new String[]{"Sensorik", "Edukatif"}},
        {"Boneka Bayi Reborn", "Boneka", 240000, 4, 10, new String[]{"Roleplay"}},
        {"Helikopter Remote Mini", "Kendaraan", 335000, 8, 15, new String[]{"Baterai", "Remote"}},
        {"Tenda Bermain Istana", "Outdoor", 420000, 2, 7, new String[]{"Indoor", "Roleplay"}},
        {"Slime Kit DIY Glitter", "Seni & Kreativitas", 78000, 5, 12, new String[]{"Sensorik", "Kreatif"}},
        {"Raket Badminton Anak Set", "Olahraga", 115000, 5, 12, new String[]{"Outdoor", "Motorik"}},
        {"Mikroskop Mainan Edukasi", "Edukatif", 230000, 7, 14, new String[]{"Edukatif", "Sains"}},
        {"Lego Duplo Kebun Binatang", "Konstruksi", 295000, 2, 5, new String[]{"Bestseller", "Balita"}},
        {"Puzzle Jigsaw Dinosaurus 200pcs", "Puzzle", 125000, 6, 12, new String[]{"Kompleks"}},
        {"Boneka Action Figure Superhero", "Boneka", 155000, 4, 12, new String[]{"Roleplay"}},
        {"Mobil Aki Anak Jeep", "Kendaraan", 1200000, 2, 6, new String[]{"Baterai", "Premium"}},
        {"Ayunan Indoor Anak", "Outdoor", 380000, 2, 7, new String[]{"Indoor", "Motorik"}},
        {"Kanvas Lukis & Cat Air Set", "Seni & Kreativitas", 105000, 5, 13, new String[]{"Kreatif"}},
        {"Set Bowling Mini Anak", "Olahraga", 88000, 3, 9, new String[]{"Indoor", "Motorik"}},
        {"Globe Dunia Berputar Edukasi", "Edukatif", 140000, 6, 14, new String[]{"Edukatif", "Sains"}},
        {"Magnetic Tiles 60pcs", "Konstruksi", 365000, 3, 10, new String[]{"Kreatif", "Bestseller"}},
        {"Puzzle Kubus Rubik 3x3", "Puzzle", 45000, 7, 15, new String[]{"Hemat", "Logika"}},
        {"Boneka Jari Cerita Dongeng", "Boneka", 62000, 2, 6, new String[]{"Roleplay", "Hemat"}},
        {"Pesawat Terbang Styrofoam", "Kendaraan", 48000, 5, 12, new String[]{"Outdoor", "Hemat"}},
        {"Kolam Bola Anak Portable", "Outdoor", 340000, 1, 5, new String[]{"Indoor", "Sensorik"}},
        {"Plastisin Lilin Malam 12 Warna", "Seni & Kreativitas", 58000, 3, 9, new String[]{"Sensorik", "Hemat"}},
        {"Skipping Rope Penghitung", "Olahraga", 55000, 6, 14, new String[]{"Outdoor", "Hemat"}},
        {"Buku Aktivitas Stiker Edukasi", "Edukatif", 48000, 3, 7, new String[]{"Edukatif", "Hemat"}},
        {"Set Konstruksi Mur & Baut", "Konstruksi", 175000, 4, 10, new String[]{"Motorik", "Kreatif"}},
        {"Puzzle Tangram Kayu Klasik", "Puzzle", 68000, 5, 12, new String[]{"Kayu", "Logika"}},
        {"Boneka Masak-masakan Set Dapur", "Boneka", 185000, 3, 8, new String[]{"Roleplay"}},
    };

    // CAT_BASE: [edukasi, usia, kreatif, populer, mudah] per category
    private static final Map<String, int[]> CAT_BASE = Map.of(
        "Edukatif", new int[]{5, 4, 4, 3, 4},
        "Outdoor", new int[]{3, 4, 3, 4, 4},
        "Puzzle", new int[]{5, 4, 5, 3, 3},
        "Boneka", new int[]{2, 4, 4, 5, 5},
        "Kendaraan", new int[]{2, 3, 3, 5, 3},
        "Konstruksi", new int[]{5, 4, 5, 3, 3},
        "Seni & Kreativitas", new int[]{4, 4, 5, 3, 4},
        "Olahraga", new int[]{3, 4, 3, 4, 5});

    private final List<Category> categories = new ArrayList<>();
    private final List<Criterion> criteria = new ArrayList<>();
    private final List<WeightProfile> profiles = new ArrayList<>();
    private final List<Toy> toys = new ArrayList<>();
    private final Map<String, Category> categoryById = new LinkedHashMap<>();
    private final Map<String, WeightProfile> profileById = new LinkedHashMap<>();

    @PostConstruct
    void seed() {
        for (String[] c : CATEGORY_DEF) {
            Category cat = new Category(c[0], c[1], c[2]);
            categories.add(cat);
            categoryById.put(cat.id(), cat);
        }
        for (CritDef c : CRITERION_DEF) {
            criteria.add(new Criterion(c.code(), c.no(), c.name(), c.type(), c.desc(), c.abbr()));
        }
        for (ProfDef p : PROFILE_DEF) {
            Map<String, Double> w = new LinkedHashMap<>();
            for (int i = 0; i < CRITERION_DEF.length; i++) {
                w.put(CRITERION_DEF[i].code(), p.w()[i]);
            }
            WeightProfile profile = new WeightProfile(
                p.id(), p.name(), p.shortName(), p.icon(), p.desc(), p.cr(), p.lambda(), p.ci(), w);
            profiles.add(profile);
            profileById.put(profile.id(), profile);
        }
        for (int i = 0; i < TOY_DEF.length; i++) {
            Object[] t = TOY_DEF[i];
            String name = (String) t[0];
            String catName = (String) t[1];
            long harga = ((Number) t[2]).longValue();
            int usiaMin = (int) t[3];
            int usiaMax = (int) t[4];
            List<String> tags = List.of((String[]) t[5]);
            int stok = (i % 11 == 3) ? 0 : 4 + ((i * 13) % 40);
            boolean aktif = (i % 17) != 9;
            String categoryId = categoryIdByName(catName);
            String deskripsi = "Mainan " + catName.toLowerCase() + " untuk anak usia "
                + usiaMin + "–" + usiaMax + " tahun.";
            toys.add(new Toy(i + 1, name, categoryId, catName, harga, usiaMin, usiaMax, tags,
                stok, aktif, deskripsi, scoreFor(catName, harga, i)));
        }
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
        return ((i * 7 + k * 13) % 3) - 1; // -1 | 0 | 1
    }

    private static Map<String, Integer> scoreFor(String catName, long harga, int i) {
        int[] b = CAT_BASE.getOrDefault(catName, CAT_BASE.get("Edukatif"));
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

    private String categoryIdByName(String name) {
        return categories.stream().filter(c -> c.name().equals(name)).findFirst()
            .map(Category::id).orElse(name);
    }

    // ── accessors ────────────────────────────────────────────────────────
    public List<Category> categories() {
        return List.copyOf(categories);
    }

    public List<Criterion> criteria() {
        return List.copyOf(criteria);
    }

    public List<WeightProfile> profiles() {
        return List.copyOf(profiles);
    }

    public List<Toy> toys() {
        return List.copyOf(toys);
    }

    public List<Toy> activeToys() {
        return toys.stream().filter(Toy::active).toList();
    }

    public Category category(String id) {
        return categoryById.get(id);
    }

    public WeightProfile profile(String id) {
        return profileById.getOrDefault(id, profileById.get("balanced"));
    }

    public Toy toy(int id) {
        return toys.stream().filter(t -> t.id() == id).findFirst().orElse(null);
    }

    public long categoryCount(String categoryId) {
        return toys.stream().filter(t -> t.categoryId().equals(categoryId)).count();
    }
}
