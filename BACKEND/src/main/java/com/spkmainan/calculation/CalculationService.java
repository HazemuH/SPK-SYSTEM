package com.spkmainan.calculation;

import com.spkmainan.ahp.SawEngine;
import com.spkmainan.calculation.CalculationDto.PrecheckItem;
import com.spkmainan.calculation.CalculationDto.PrecheckResponse;
import com.spkmainan.calculation.CalculationDto.ProfileDetail;
import com.spkmainan.calculation.CalculationDto.ProfileSummary;
import com.spkmainan.calculation.CalculationDto.RankingRow;
import com.spkmainan.calculation.CalculationDto.RunDetail;
import com.spkmainan.calculation.CalculationDto.RunSummary;
import com.spkmainan.common.exception.BadRequestException;
import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.domain.Criterion;
import com.spkmainan.domain.CriterionType;
import com.spkmainan.domain.DomainCatalog;
import com.spkmainan.domain.Toy;
import com.spkmainan.domain.WeightProfile;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs the SAW synthesis for every weight profile, persists the ranking snapshot
 * (session), and publishes it. AHP consistency comes from each profile's stored CR.
 */
@Service
public class CalculationService {

    private final CalculationRunRepository runs;
    private final DomainCatalog catalog;
    private final SawEngine saw;

    public CalculationService(CalculationRunRepository runs, DomainCatalog catalog, SawEngine saw) {
        this.runs = runs;
        this.catalog = catalog;
        this.saw = saw;
    }

    // ── pre-check ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PrecheckResponse precheck() {
        List<WeightProfile> profiles = catalog.profiles();
        List<Criterion> criteria = catalog.activeCriteria();
        List<Toy> toys = catalog.activeToys();
        List<String> benefit = criteria.stream()
            .filter(c -> c.type() == CriterionType.BENEFIT).map(Criterion::code).toList();

        boolean allConsistent = !profiles.isEmpty() && profiles.stream().allMatch(WeightProfile::consistent);
        boolean ratingsOk = !toys.isEmpty() && toys.stream().allMatch(t ->
            benefit.stream().allMatch(b -> {
                Integer v = t.scores().get(b);
                return v != null && v >= 1 && v <= 5;
            }));
        boolean priceOk = !toys.isEmpty() && toys.stream().allMatch(t -> t.price() > 0);
        boolean dataOk = !toys.isEmpty() && !catalog.categories().isEmpty();

        List<PrecheckItem> items = List.of(
            new PrecheckItem("Pairwise kriteria — " + profiles.size() + " profil", allConsistent,
                allConsistent ? "Semua CR ≤ 0,10 (AHP)" : "Ada profil dengan CR > 0,10"),
            new PrecheckItem("Penilaian alternatif (rating 1–5)", ratingsOk,
                toys.size() + " mainan × " + benefit.size() + " kriteria benefit"),
            new PrecheckItem("Harga (cost) terisi", priceOk, "Dinormalisasi min/x saat SAW"),
            new PrecheckItem("Data mainan & kategori lengkap", dataOk,
                toys.size() + " mainan · " + catalog.categories().size() + " kategori"));

        boolean allOk = items.stream().allMatch(PrecheckItem::ok);
        return new PrecheckResponse(allOk, items);
    }

    // ── run ──────────────────────────────────────────────────────────────
    @Transactional
    public RunSummary run() {
        PrecheckResponse pre = precheck();
        if (!pre.allOk()) {
            String failing = pre.items().stream()
                .filter(i -> !i.ok())
                .map(PrecheckItem::label)
                .collect(java.util.stream.Collectors.joining("; "));
            throw new BadRequestException("Kalkulasi ditolak — perbaiki dulu: " + failing);
        }
        List<Criterion> criteria = catalog.activeCriteria();
        List<Toy> active = catalog.activeToys();
        Map<Integer, Map<String, Double>> norm = saw.normalize(active, criteria);

        String code = String.format("%03d", runs.count() + 1);
        CalculationRun run = new CalculationRun(code, Instant.now(), active.size());

        for (WeightProfile profile : catalog.profiles()) {
            CalculationResult result = new CalculationResult(profile.id(), profile.name(),
                profile.cr(), profile.lambdaMax(), profile.ci(), profile.consistent());

            List<Toy> sorted = new ArrayList<>(active);
            sorted.sort(Comparator.comparingDouble(
                (Toy t) -> saw.score(norm.getOrDefault(t.id(), Map.of()), profile.weights())).reversed());

            for (int i = 0; i < sorted.size(); i++) {
                Toy t = sorted.get(i);
                double s = saw.score(norm.getOrDefault(t.id(), Map.of()), profile.weights());
                result.addRanking(new RankingEntry(t.id(), t.name(), t.categoryName(), i + 1, s));
            }
            if (!sorted.isEmpty()) {
                result.setBestToyId(sorted.get(0).id());
                result.setBestToyName(sorted.get(0).name());
            }
            run.addResult(result);
        }
        return summary(runs.save(run));
    }

    /** Used by the seeder: produce and publish an initial session out of the box. */
    @Transactional
    public RunSummary runAndPublish() {
        RunSummary s = run();
        return publish(s.id());
    }

    // ── publish ──────────────────────────────────────────────────────────
    @Transactional
    public RunSummary publish(Long id) {
        CalculationRun run = getOrThrow(id);
        runs.findAll().forEach(r -> {
            if (r.isPublished() && !r.getId().equals(id)) {
                r.setPublished(false);
            }
        });
        run.setPublished(true);
        run.setPublishedAt(Instant.now());
        return summary(runs.save(run));
    }

    // ── read ───────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<RunSummary> list() {
        return runs.findAllByOrderByRunAtDesc().stream().map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public RunDetail detail(Long id) {
        CalculationRun run = getOrThrow(id);
        List<ProfileDetail> details = run.getResults().stream().map(r -> {
            List<RankingRow> ranking = r.getRankings().stream()
                .sorted(Comparator.comparingInt(RankingEntry::getRankNo))
                .map(e -> new RankingRow(e.getRankNo(), e.getToyId(), e.getToyName(),
                    e.getCategoryName(), e.getSawScore()))
                .toList();
            return new ProfileDetail(r.getProfileCode(), r.getProfileName(), r.getCr(),
                r.getLambdaMax(), r.getCi(), r.isConsistent(), ranking);
        }).toList();
        return new RunDetail(run.getId(), run.getCode(), run.getRunAt(), run.getAltCount(),
            run.isPublished(), run.getPublishedAt(), details);
    }

    // ── helpers ────────────────────────────────────────────────────────────
    private CalculationRun getOrThrow(Long id) {
        return runs.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sesi kalkulasi tidak ditemukan: " + id));
    }

    private RunSummary summary(CalculationRun run) {
        List<ProfileSummary> profiles = run.getResults().stream()
            .map(r -> new ProfileSummary(r.getProfileCode(), r.getProfileName(), r.getCr(),
                r.isConsistent(), r.getBestToyName()))
            .toList();
        return new RunSummary(run.getId(), run.getCode(), run.getRunAt(), run.getAltCount(),
            run.isPublished(), run.getPublishedAt(), profiles);
    }
}
