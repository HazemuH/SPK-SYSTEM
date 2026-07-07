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
 * Populates the SPK domain from {@link DomainSeed} on first startup (when the
 * tables are empty). Idempotent, runs in all profiles so reference data + the
 * demo catalog exist out of the box.
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
        if (categories.count() > 0) {
            return;
        }
        categories.saveAll(DomainSeed.categories());
        criteria.saveAll(DomainSeed.criteria());
        profiles.saveAll(DomainSeed.weightProfiles());
        toys.saveAll(DomainSeed.toys());
        log.info("Seeded SPK domain: {} categories, {} criteria, {} profiles, {} toys",
            categories.count(), criteria.count(), profiles.count(), toys.count());

        // Produce & publish an initial session so reports/mobile have data out of the box.
        calculations.runAndPublish();
        log.info("Seeded & published initial calculation session");
    }
}
