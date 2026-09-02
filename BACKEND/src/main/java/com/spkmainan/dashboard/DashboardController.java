package com.spkmainan.dashboard;

import com.spkmainan.calculation.CalculationDto.PublishStatus;
import com.spkmainan.calculation.CalculationDto.RunSummary;
import com.spkmainan.calculation.CalculationService;
import com.spkmainan.domain.DomainCatalog;
import com.spkmainan.publicapi.CatalogService;
import com.spkmainan.publicapi.PublicDto.RankedToy;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin dashboard summary (auth): counts, category distribution, overall top-10, recent sessions. */
@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Admin dashboard summary (auth)")
public class DashboardController {

    private final DomainCatalog catalog;
    private final CatalogService catalogService;
    private final CalculationService calculations;

    public DashboardController(DomainCatalog catalog, CatalogService catalogService,
                              CalculationService calculations) {
        this.catalog = catalog;
        this.catalogService = catalogService;
        this.calculations = calculations;
    }

    public record CategorySlice(String name, long count) {}

    public record TopToy(int rank, String name, String category, double score) {}

    public record Summary(
            int totalToys, int totalCriteria, int totalCategories, int totalProfiles,
            List<CategorySlice> categoryDistribution, List<TopToy> topOverall,
            List<RunSummary> recentSessions, PublishStatus publishStatus) {}

    /** How many alternatives the overall ranking card shows. */
    private static final int TOP_LIMIT = 10;

    @GetMapping("/summary")
    public Summary summary() {
        List<CategorySlice> dist = catalog.categories().stream()
            .map(c -> new CategorySlice(c.name(), catalog.categoryCount(c.id())))
            .toList();
        // One ranking over every alternative under the global AHP criteria weights
        // (null profile = the default one), not a single scenario profile.
        List<TopToy> topOverall = catalogService.top(null, TOP_LIMIT).stream()
            .map(r -> new TopToy(r.rank(), r.toy().name(), r.toy().category(), r.score()))
            .toList();
        List<RunSummary> recent = calculations.list().stream().limit(5).toList();

        return new Summary(catalog.toys().size(), catalog.criteria().size(),
            catalog.categories().size(), catalog.profiles().size(), dist, topOverall, recent,
            calculations.publishStatus());
    }
}
