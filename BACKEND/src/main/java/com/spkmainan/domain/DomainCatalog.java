package com.spkmainan.domain;

import com.spkmainan.ahp.AhpSynthesisEngine;
import com.spkmainan.category.CategoryEntity;
import com.spkmainan.category.CategoryRepository;
import com.spkmainan.criterion.CriterionEntity;
import com.spkmainan.criterion.CriterionLevelEntity;
import com.spkmainan.criterion.CriterionLevelRepository;
import com.spkmainan.criterion.CriterionRepository;
import com.spkmainan.toy.ToyEntity;
import com.spkmainan.toy.ToyRepository;
import com.spkmainan.weightprofile.WeightProfileEntity;
import com.spkmainan.weightprofile.WeightProfileRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads the persisted SPK domain and maps entities to the immutable value
 * records the AHP engine and public API consume. Single seam between
 * persistence and the calculation/read side.
 */
@Component
@Transactional(readOnly = true)
public class DomainCatalog {

    private static final String DEFAULT_PROFILE = "balanced";

    private final CategoryRepository categoryRepo;
    private final CriterionRepository criterionRepo;
    private final CriterionLevelRepository levelRepo;
    private final WeightProfileRepository profileRepo;
    private final ToyRepository toyRepo;

    public DomainCatalog(CategoryRepository categoryRepo, CriterionRepository criterionRepo,
                         CriterionLevelRepository levelRepo,
                         WeightProfileRepository profileRepo, ToyRepository toyRepo) {
        this.categoryRepo = categoryRepo;
        this.criterionRepo = criterionRepo;
        this.levelRepo = levelRepo;
        this.profileRepo = profileRepo;
        this.toyRepo = toyRepo;
    }

    public List<Category> categories() {
        return categoryRepo.findAll().stream()
            .map(c -> new Category(c.getCode(), c.getName(), c.getDescription()))
            .toList();
    }

    public List<Criterion> criteria() {
        return criterionRepo.findAllByOrderByNoAsc().stream()
            .map(this::toCriterion)
            .toList();
    }

    /** Only active criteria (archived/inactive are excluded from the AHP synthesis). */
    public List<Criterion> activeCriteria() {
        return criterionRepo.findAllByOrderByNoAsc().stream()
            .filter(CriterionEntity::isActive)
            .map(this::toCriterion)
            .toList();
    }

    private Criterion toCriterion(CriterionEntity c) {
        return new Criterion(c.getCode(), c.getNo(), c.getName(), c.getType(),
            c.getDescription(), c.getAbbr());
    }

    /** Every criterion's subcriteria (S1–S5), ordered by criterion then level. */
    public List<CriterionLevel> criterionLevels() {
        return levelRepo.findAllByOrderByCriterionCodeAscLevelAsc().stream()
            .map(l -> new CriterionLevel(l.getCriterionCode(), l.getLevel(), l.getLabel(),
                l.getPriority()))
            .toList();
    }

    /** Criterion code → the five local priorities, S1 first — what the synthesis reads. */
    public Map<String, double[]> levelPriorities() {
        Map<String, double[]> out = new LinkedHashMap<>();
        for (CriterionLevelEntity l : levelRepo.findAllByOrderByCriterionCodeAscLevelAsc()) {
            double[] byLevel = out.computeIfAbsent(l.getCriterionCode(),
                k -> new double[AhpSynthesisEngine.LEVELS]);
            if (l.getLevel() >= 1 && l.getLevel() <= AhpSynthesisEngine.LEVELS) {
                byLevel[l.getLevel() - 1] = l.getPriority();
            }
        }
        return out;
    }

    public List<WeightProfile> profiles() {
        return profileRepo.findAll().stream().map(this::toProfile).toList();
    }

    public List<Toy> toys() {
        Map<String, String> catNames = categoryNames();
        return toyRepo.findAll().stream().map(t -> toToy(t, catNames)).toList();
    }

    public List<Toy> activeToys() {
        return toys().stream().filter(Toy::active).toList();
    }

    public WeightProfile profile(String code) {
        return profileRepo.findByCode(code).map(this::toProfile)
            .orElseGet(() -> profileRepo.findByCode(DEFAULT_PROFILE).map(this::toProfile).orElse(null));
    }

    public Toy toy(int id) {
        return toyRepo.findById((long) id)
            .map(t -> toToy(t, categoryNames())).orElse(null);
    }

    public long categoryCount(String categoryCode) {
        return toyRepo.findAll().stream()
            .filter(t -> t.getCategoryCode().equals(categoryCode)).count();
    }

    private Map<String, String> categoryNames() {
        Map<String, String> names = new LinkedHashMap<>();
        for (CategoryEntity c : categoryRepo.findAll()) {
            names.put(c.getCode(), c.getName());
        }
        return names;
    }

    private WeightProfile toProfile(WeightProfileEntity e) {
        return new WeightProfile(e.getCode(), e.getName(), e.getShortName(), e.getIcon(),
            e.getDescription(), e.getCr(), e.getLambdaMax(), e.getCi(),
            new LinkedHashMap<>(e.getWeights()));
    }

    private Toy toToy(ToyEntity t, Map<String, String> catNames) {
        return new Toy((int) (long) t.getId(), t.getName(), t.getCategoryCode(),
            catNames.getOrDefault(t.getCategoryCode(), t.getCategoryCode()), t.getPrice(),
            t.getAgeMin(), t.getAgeMax(), List.copyOf(t.getTags()), t.getStock(), t.isActive(),
            t.getDescription(), new LinkedHashMap<>(t.getScores()));
    }
}
