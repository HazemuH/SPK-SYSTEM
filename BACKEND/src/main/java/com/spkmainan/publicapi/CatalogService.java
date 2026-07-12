package com.spkmainan.publicapi;

import com.spkmainan.ahp.SawEngine;
import com.spkmainan.calculation.CalculationCriterion;
import com.spkmainan.calculation.CalculationNorm;
import com.spkmainan.calculation.CalculationResult;
import com.spkmainan.calculation.CalculationRun;
import com.spkmainan.calculation.CalculationRunRepository;
import com.spkmainan.calculation.RankingEntry;
import com.spkmainan.domain.Criterion;
import com.spkmainan.domain.CriterionType;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-side service backing the public (mobile) API. It serves the latest
 * <b>published</b> calculation run: SAW scores, ranking, weights, and normalized
 * values (r_ij) are frozen at publish time (the publish gate). Toy display
 * attributes (name/price/stock/…) are hydrated live per toy_id, so a shop's
 * current price/stock shows through while the decision output stays frozen until
 * the admin re-runs + re-publishes. No published run → everything is empty.
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
    private final CalculationRunRepository runs;

    public CatalogService(DomainCatalog catalog, SawEngine saw, CalculationRunRepository runs) {
        this.catalog = catalog;
        this.saw = saw;
        this.runs = runs;
    }

    /**
     * A published, frozen view of the decision: criteria/norm/weights come from the
     * latest published run; toys are the frozen set hydrated with <b>live</b> attributes
     * (deleted toys dropped). {@code present == false} means nothing is published.
     */
    private record PublishedSnapshot(boolean present, List<Criterion> criteria, List<Toy> toys,
                                     Map<Integer, Toy> toyById,
                                     Map<Integer, Map<String, Double>> norm,
                                     Map<String, WeightProfile> profiles) {

        static PublishedSnapshot empty() {
            return new PublishedSnapshot(false, List.of(), List.of(), Map.of(), Map.of(), Map.of());
        }

        /** Frozen profile by code, falling back to the default, then any available profile. */
        WeightProfile profileOrDefault(String code) {
            if (code != null && profiles.containsKey(code)) {
                return profiles.get(code);
            }
            if (profiles.containsKey(DEFAULT_PROFILE)) {
                return profiles.get(DEFAULT_PROFILE);
            }
            return profiles.values().stream().findFirst().orElse(null);
        }
    }

    /** Build the frozen snapshot from the latest published run (empty if none published). */
    private PublishedSnapshot buildSnapshot() {
        Optional<CalculationRun> pub = runs.findFirstByPublishedTrueOrderByPublishedAtDesc();
        if (pub.isEmpty()) {
            return PublishedSnapshot.empty();
        }
        CalculationRun run = pub.get();

        List<Criterion> criteria = run.getCriteria().stream()
            .sorted(Comparator.comparingInt(CalculationCriterion::getNo))
            .map(c -> new Criterion(c.getCode(), c.getNo(), c.getName(),
                CriterionType.valueOf(c.getType()), null, c.getAbbr()))
            .toList();

        // Frozen r_ij, keeping only toys that still exist live (deleted toys are dropped).
        Map<Integer, Map<String, Double>> norm = new LinkedHashMap<>();
        Map<Integer, Toy> toyById = new LinkedHashMap<>();
        for (CalculationNorm n : run.getNorms()) {
            Toy live = toyById.get(n.getToyId());
            if (live == null && !toyById.containsKey(n.getToyId())) {
                live = catalog.toy(n.getToyId());
                toyById.put(n.getToyId(), live);   // may be null (deleted) — cached to avoid re-query
            }
            if (toyById.get(n.getToyId()) == null) {
                continue;   // deleted toy: skip its frozen values
            }
            norm.computeIfAbsent(n.getToyId(), k -> new LinkedHashMap<>())
                .put(n.getCriterionCode(), n.getNormValue());
        }
        toyById.values().removeIf(t -> t == null);
        List<Toy> toys = new ArrayList<>(toyById.values());

        // Frozen profiles: weights + cr/name/shortName/icon.
        Map<String, WeightProfile> profiles = new LinkedHashMap<>();
        for (CalculationResult r : run.getResults()) {
            Map<String, Double> w = new LinkedHashMap<>();
            r.getWeights().forEach(cw -> w.put(cw.getCriterionCode(), cw.getWeight()));
            profiles.put(r.getProfileCode(), new WeightProfile(
                r.getProfileCode(), r.getProfileName(), r.getShortName(), r.getIcon(),
                null, r.getCr(), r.getLambdaMax(), r.getCi(), w));
        }
        return new PublishedSnapshot(true, criteria, toys, toyById, norm, profiles);
    }

    private double normValue(PublishedSnapshot s, int toyId, String critCode) {
        return s.norm().getOrDefault(toyId, Map.of()).getOrDefault(critCode, 0.0);
    }

    private double score(PublishedSnapshot s, Toy toy, WeightProfile profile) {
        return saw.score(s.norm().getOrDefault(toy.id(), Map.of()), profile.weights());
    }

    private List<RankedToy> rank(PublishedSnapshot s, List<Toy> toys, WeightProfile profile) {
        if (profile == null) {
            return List.of();
        }
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
    @Transactional(readOnly = true)
    public Meta meta() {
        PublishedSnapshot s = buildSnapshot();
        // Categories are structural filter labels — always live. Criteria/profiles are frozen.
        List<CategoryView> cats = catalog.categories().stream()
            .map(c -> new CategoryView(c.id(), c.name(), c.description(), catalog.categoryCount(c.id())))
            .toList();
        var lastPublishedAt = runs.findFirstByPublishedTrueOrderByPublishedAtDesc()
            .map(CalculationRun::getPublishedAt).orElse(null);
        return new Meta(cats, s.criteria().stream().map(this::criterionView).toList(),
            SORT_OPTIONS, s.profiles().values().stream().map(this::profileView).toList(),
            lastPublishedAt);
    }

    @Transactional(readOnly = true)
    public List<ProfileView> profiles() {
        return buildSnapshot().profiles().values().stream().map(this::profileView).toList();
    }

    @Transactional(readOnly = true)
    public List<RankedToy> top(String profileId, int limit) {
        PublishedSnapshot s = buildSnapshot();
        return rank(s, s.toys(), s.profileOrDefault(profileId)).stream()
            .limit(Math.max(0, limit)).toList();
    }

    @Transactional(readOnly = true)
    public List<RankedToy> catalog(String profileId, String sortCode, String categoryId,
                                   boolean inStock, String search) {
        PublishedSnapshot s = buildSnapshot();
        List<RankedToy> ranked;
        if (sortCode != null && !sortCode.isBlank()) {
            List<Toy> sorted = new ArrayList<>(s.toys());
            sorted.sort(Comparator.comparingDouble((Toy t) -> normValue(s, t.id(), sortCode)).reversed());
            ranked = new ArrayList<>();
            for (int i = 0; i < sorted.size(); i++) {
                Toy t = sorted.get(i);
                ranked.add(new RankedToy(i + 1, normValue(s, t.id(), sortCode), toyView(t)));
            }
        } else {
            ranked = rank(s, s.toys(), s.profileOrDefault(profileId));
        }
        String q = search == null ? "" : search.toLowerCase();
        return ranked.stream()
            .filter(r -> q.isBlank() || r.toy().name().toLowerCase().contains(q))
            .filter(r -> categoryId == null || categoryId.isBlank() || r.toy().categoryId().equals(categoryId))
            .filter(r -> !inStock || r.toy().stock() > 0)
            .toList();
    }

    @Transactional(readOnly = true)
    public ToyDetail detail(int toyId) {
        PublishedSnapshot s = buildSnapshot();
        Toy toy = s.toyById().get(toyId);
        if (toy == null) {
            return null;   // not in the published snapshot (or deleted) → gated out
        }
        WeightProfile balanced = s.profileOrDefault(DEFAULT_PROFILE);
        List<RankedToy> global = rank(s, s.toys(), balanced);
        int globalRank = global.stream().filter(r -> r.toy().id() == toyId).findFirst()
            .map(RankedToy::rank).orElse(0);
        double sawScore = balanced == null ? 0.0 : score(s, toy, balanced);

        List<Toy> sameCat = s.toys().stream()
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

    @Transactional(readOnly = true)
    public Recommendation recommend(String usia, String budget, String tujuan, String prioritas) {
        PublishedSnapshot s = buildSnapshot();
        int[] age = AGE_MAP.getOrDefault(usia, new int[]{0, 99});
        long budgetMax = parseBudget(budget);
        // `prioritas` is a published weight-profile code chosen by the user (1:1 with the AHP
        // output). Fall back to the legacy keyword→scenario map for older clients; unknown codes
        // resolve to the default profile inside the snapshot.
        String profileCode = prioritas == null ? DEFAULT_PROFILE
            : PRIO_SCENARIO.getOrDefault(prioritas, prioritas);
        WeightProfile profile = s.profileOrDefault(profileCode);
        if (profile == null) {
            return new Recommendation(profileCode, "", 0, true, List.of(), List.of());
        }

        List<Toy> base = s.toys().stream()
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

    @Transactional(readOnly = true)
    public CompareResult compare(List<Integer> toyIds, String profileId) {
        PublishedSnapshot s = buildSnapshot();
        WeightProfile profile = s.profileOrDefault(profileId);
        List<Toy> toys = toyIds.stream().map(id -> s.toyById().get(id)).filter(t -> t != null).toList();
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
            winScore = Math.max(winScore, profile == null ? 0.0 : score(s, t, profile));
        }
        List<CompareTotal> totals = new ArrayList<>();
        for (Toy t : toys) {
            double sc = profile == null ? 0.0 : score(s, t, profile);
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
