package com.spkmainan.publicapi;

import com.spkmainan.ahp.SawEngine;
import com.spkmainan.domain.Category;
import com.spkmainan.domain.Criterion;
import com.spkmainan.domain.DomainCatalog;
import com.spkmainan.domain.Toy;
import com.spkmainan.domain.WeightProfile;
import com.spkmainan.publicapi.PublicDto.CategoryView;
import com.spkmainan.publicapi.PublicDto.CompareCell;
import com.spkmainan.publicapi.PublicDto.CompareResult;
import com.spkmainan.publicapi.PublicDto.CompareRow;
import com.spkmainan.publicapi.PublicDto.CompareTotal;
import com.spkmainan.publicapi.PublicDto.CriterionView;
import com.spkmainan.publicapi.PublicDto.Meta;
import com.spkmainan.publicapi.PublicDto.ProfileView;
import com.spkmainan.publicapi.PublicDto.RankedToy;
import com.spkmainan.publicapi.PublicDto.Recommendation;
import com.spkmainan.publicapi.PublicDto.SortOption;
import com.spkmainan.publicapi.PublicDto.ToyDetail;
import com.spkmainan.publicapi.PublicDto.ToyView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Read-side service backing the public (mobile) API. Reads the persisted domain
 * via {@link DomainCatalog}, runs SAW normalization + weighted sum, and filters/
 * sorts/compares. It never runs pairwise/CR (that's the admin side). Ported from
 * the design's data.jsx query helpers.
 */
@Service
public class CatalogService {

    private static final String DEFAULT_PROFILE = "balanced";

    private static final Map<String, int[]> AGE_MAP = Map.of(
        "0-2", new int[]{0, 2}, "3-5", new int[]{3, 5}, "6-8", new int[]{6, 8},
        "9-12", new int[]{9, 12}, "13+", new int[]{13, 99});

    private static final Map<String, List<String>> TUJUAN_CAT = Map.of(
        "edukatif", List.of("edukatif", "puzzle", "konstruksi"),
        "aktif", List.of("outdoor", "olahraga", "kendaraan"),
        "kreatif", List.of("seni", "konstruksi", "puzzle"),
        "hiburan", List.of("boneka", "kendaraan", "konstruksi"));

    private static final Map<String, String> PRIO_SCENARIO = Map.of(
        "keamanan", "safety", "edukasi", "education", "harga", "price", "tahan", "durability");

    private static final List<SortOption> SORT_OPTIONS = List.of(
        new SortOption(null, "Skor keseluruhan"),
        new SortOption("keamanan", "Paling aman"),
        new SortOption("edukasi", "Paling edukatif"),
        new SortOption("harga", "Paling murah"),
        new SortOption("tahan", "Paling awet"),
        new SortOption("kreatif", "Paling kreatif"));

    private final DomainCatalog catalog;
    private final SawEngine saw;

    public CatalogService(DomainCatalog catalog, SawEngine saw) {
        this.catalog = catalog;
        this.saw = saw;
    }

    /** Per-request view of the domain + its SAW normalization (over active toys). */
    private record Snapshot(List<Criterion> criteria, List<Toy> activeToys,
                            Map<Integer, Map<String, Double>> norm) {}

    private Snapshot snapshot() {
        List<Criterion> criteria = catalog.criteria();
        List<Toy> active = catalog.activeToys();
        return new Snapshot(criteria, active, saw.normalize(active, criteria));
    }

    private double normValue(Snapshot s, int toyId, String critCode) {
        return s.norm().getOrDefault(toyId, Map.of()).getOrDefault(critCode, 0.0);
    }

    private double score(Snapshot s, Toy toy, WeightProfile profile) {
        return saw.score(s.norm().getOrDefault(toy.id(), Map.of()), profile.weights());
    }

    private List<RankedToy> rank(Snapshot s, List<Toy> toys, WeightProfile profile) {
        List<Toy> sorted = new ArrayList<>(toys);
        sorted.sort(Comparator.comparingDouble((Toy t) -> score(s, t, profile)).reversed());
        List<RankedToy> out = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Toy t = sorted.get(i);
            out.add(new RankedToy(i + 1, score(s, t, profile), toyView(t)));
        }
        return out;
    }

    // ── public queries ───────────────────────────────────────────────────
    public Meta meta() {
        List<CategoryView> cats = catalog.categories().stream()
            .map(c -> new CategoryView(c.id(), c.name(), c.description(), catalog.categoryCount(c.id())))
            .toList();
        return new Meta(cats, catalog.criteria().stream().map(this::criterionView).toList(),
            SORT_OPTIONS, profiles());
    }

    public List<ProfileView> profiles() {
        return catalog.profiles().stream().map(this::profileView).toList();
    }

    public List<RankedToy> top(String profileId, int limit) {
        Snapshot s = snapshot();
        return rank(s, s.activeToys(), catalog.profile(profileId)).stream()
            .limit(Math.max(0, limit)).toList();
    }

    public List<RankedToy> catalog(String profileId, String sortCode, String categoryId,
                                   boolean inStock, String search) {
        Snapshot s = snapshot();
        List<RankedToy> ranked;
        if (sortCode != null && !sortCode.isBlank()) {
            List<Toy> sorted = new ArrayList<>(s.activeToys());
            sorted.sort(Comparator.comparingDouble((Toy t) -> normValue(s, t.id(), sortCode)).reversed());
            ranked = new ArrayList<>();
            for (int i = 0; i < sorted.size(); i++) {
                Toy t = sorted.get(i);
                ranked.add(new RankedToy(i + 1, normValue(s, t.id(), sortCode), toyView(t)));
            }
        } else {
            ranked = rank(s, s.activeToys(), catalog.profile(profileId));
        }
        String q = search == null ? "" : search.toLowerCase();
        return ranked.stream()
            .filter(r -> q.isBlank() || r.toy().name().toLowerCase().contains(q))
            .filter(r -> categoryId == null || categoryId.isBlank() || r.toy().categoryId().equals(categoryId))
            .filter(r -> !inStock || r.toy().stock() > 0)
            .toList();
    }

    public ToyDetail detail(int toyId) {
        Toy toy = catalog.toy(toyId);
        if (toy == null) {
            return null;
        }
        Snapshot s = snapshot();
        WeightProfile balanced = catalog.profile(DEFAULT_PROFILE);
        List<RankedToy> global = rank(s, s.activeToys(), balanced);
        int globalRank = global.stream().filter(r -> r.toy().id() == toyId).findFirst()
            .map(RankedToy::rank).orElse(0);
        double sawScore = score(s, toy, balanced);

        List<Toy> sameCat = s.activeToys().stream()
            .filter(t -> t.categoryId().equals(toy.categoryId())).toList();
        List<RankedToy> catRanked = rank(s, sameCat, balanced);
        int catRank = catRanked.stream().filter(r -> r.toy().id() == toyId).findFirst()
            .map(RankedToy::rank).orElse(0);

        Map<String, Double> row = s.norm().getOrDefault(toyId, Map.of());
        List<Criterion> byStrength = new ArrayList<>(s.criteria());
        byStrength.sort(Comparator.comparingDouble((Criterion c) -> row.getOrDefault(c.code(), 0.0)).reversed());
        List<CriterionView> strengths = byStrength.stream().limit(2).map(this::criterionView).toList();
        List<CriterionView> weaknesses = byStrength.stream()
            .skip(Math.max(0, byStrength.size() - 1)).map(this::criterionView).toList();

        ToyView nextBest = null;
        if (toy.stock() == 0) {
            nextBest = catRanked.stream()
                .filter(r -> r.toy().id() != toyId && r.toy().stock() > 0)
                .findFirst().map(RankedToy::toy).orElse(null);
        }

        return new ToyDetail(new RankedToy(globalRank, sawScore, toyView(toy)),
            globalRank, catRank, sameCat.size(), row, strengths, weaknesses, nextBest);
    }

    public Recommendation recommend(String usia, String budget, String tujuan, String prioritas) {
        Snapshot s = snapshot();
        int[] age = AGE_MAP.getOrDefault(usia, new int[]{0, 99});
        long budgetMax = parseBudget(budget);
        WeightProfile profile = catalog.profile(PRIO_SCENARIO.getOrDefault(prioritas, DEFAULT_PROFILE));

        List<Toy> base = s.activeToys().stream()
            .filter(t -> t.ageMin() <= age[1] && t.ageMax() >= age[0] && t.price() <= budgetMax)
            .toList();
        List<RankedToy> ranked = rank(s, base, profile);

        List<String> preferCats = TUJUAN_CAT.getOrDefault(tujuan, List.of());
        List<RankedToy> primary = reRank(ranked.stream()
            .filter(r -> preferCats.contains(r.toy().categoryId())).toList());
        List<RankedToy> others = reRank(ranked.stream()
            .filter(r -> !preferCats.contains(r.toy().categoryId())).toList());

        return new Recommendation(profile.id(), profile.name(), base.size(),
            primary.size() < 5, primary, others);
    }

    public CompareResult compare(List<Integer> toyIds, String profileId) {
        Snapshot s = snapshot();
        WeightProfile profile = catalog.profile(profileId);
        List<Toy> toys = toyIds.stream().map(catalog::toy).filter(t -> t != null).toList();
        List<ToyView> views = toys.stream().map(this::toyView).toList();

        List<CompareRow> rows = new ArrayList<>();
        for (Criterion c : s.criteria()) {
            double best = Double.NEGATIVE_INFINITY;
            for (Toy t : toys) {
                best = Math.max(best, normValue(s, t.id(), c.code()));
            }
            List<CompareCell> cells = new ArrayList<>();
            for (Toy t : toys) {
                double v = normValue(s, t.id(), c.code());
                cells.add(new CompareCell(t.id(), v, v == best && best > 0));
            }
            rows.add(new CompareRow(criterionView(c), cells));
        }

        double winScore = Double.NEGATIVE_INFINITY;
        for (Toy t : toys) {
            winScore = Math.max(winScore, score(s, t, profile));
        }
        List<CompareTotal> totals = new ArrayList<>();
        for (Toy t : toys) {
            double sc = score(s, t, profile);
            totals.add(new CompareTotal(t.id(), sc, sc == winScore));
        }
        return new CompareResult(views, rows, totals);
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private List<RankedToy> reRank(List<RankedToy> list) {
        List<RankedToy> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            out.add(new RankedToy(i + 1, list.get(i).score(), list.get(i).toy()));
        }
        return out;
    }

    private static long parseBudget(String budget) {
        try {
            return budget == null || budget.isBlank() ? 99_999_999L : Long.parseLong(budget.trim());
        } catch (NumberFormatException e) {
            return 99_999_999L;
        }
    }

    private ToyView toyView(Toy t) {
        return new ToyView(t.id(), t.name(), t.categoryId(), t.categoryName(), t.price(),
            t.ageMin(), t.ageMax(), t.stock(), t.active(), t.tags());
    }

    private CriterionView criterionView(Criterion c) {
        return new CriterionView(c.code(), c.no(), c.name(), c.type().name().toLowerCase(),
            c.description(), c.abbr());
    }

    private ProfileView profileView(WeightProfile p) {
        return new ProfileView(p.id(), p.name(), p.shortName(), p.icon(), p.description(),
            p.cr(), p.consistent(), p.weights());
    }
}
