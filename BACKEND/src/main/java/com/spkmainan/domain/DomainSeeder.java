package com.spkmainan.domain;

import com.spkmainan.calculation.CalculationService;
import com.spkmainan.category.CategoryRepository;
import com.spkmainan.criterion.CriterionRepository;
import com.spkmainan.toy.ToyRepository;
import com.spkmainan.weightprofile.WeightProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Populates the SPK domain from {@link DomainSeed} on startup, per table, whenever
 * that table is empty. Idempotent, runs in all profiles so reference data + the
 * catalog exist out of the box; re-seeding the toys also recomputes and publishes
 * a calculation session.
 */
@Component
@Order(1)
public class DomainSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DomainSeeder.class);

    private final CategoryRepository categories;
    private final CriterionRepository criteria;
    private final WeightProfileRepository profiles;
    private final ToyRepository toys;
    private final CalculationService calculations;

    public DomainSeeder(CategoryRepository categories, CriterionRepository criteria,
                        WeightProfileRepository profiles, ToyRepository toys,
                        CalculationService calculations) {
        this.categories = categories;
        this.criteria = criteria;
        this.profiles = profiles;
        this.toys = toys;
        this.calculations = calculations;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Per-table so a reference-data reset (e.g. the V5 catalog reset) re-seeds
        // just what was wiped instead of nothing at all.
        boolean seededReference = false;
        if (categories.count() == 0) {
            categories.saveAll(DomainSeed.categories());
            seededReference = true;
        }
        if (criteria.count() == 0) {
            criteria.saveAll(DomainSeed.criteria());
            seededReference = true;
        }
        if (profiles.count() == 0) {
            profiles.saveAll(DomainSeed.weightProfiles());
            seededReference = true;
        }
        boolean seededToys = false;
        if (toys.count() == 0) {
            toys.saveAll(DomainSeed.toys());
            seededToys = true;
        }
        if (!seededReference && !seededToys) {
            return;
        }
        log.info("Seeded SPK domain: {} categories, {} criteria, {} profiles, {} toys",
            categories.count(), criteria.count(), profiles.count(), toys.count());

        // A fresh alternative set invalidates every earlier ranking, so recompute
        // and publish so reports/mobile immediately reflect the new catalog.
        if (seededToys) {
            calculations.runAndPublish();
            log.info("Recomputed & published calculation session for {} alternatives", toys.count());
        }
    }
}
