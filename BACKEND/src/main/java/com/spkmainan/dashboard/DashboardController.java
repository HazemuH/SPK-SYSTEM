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

/** Admin dashboard summary (auth): counts, category distribution, top-5, recent sessions. */
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

    public record TopToy(String name, double score) {}

    public record Summary(
            int totalToys, int totalCriteria, int totalCategories, int totalProfiles,
            List<CategorySlice> categoryDistribution, List<TopToy> top5,
            List<RunSummary> recentSessions, PublishStatus publishStatus) {}

    @GetMapping("/summary")
    public Summary summary() {
        List<CategorySlice> dist = catalog.categories().stream()
            .map(c -> new CategorySlice(c.name(), catalog.categoryCount(c.id())))
            .toList();
        List<TopToy> top5 = catalogService.top("balanced", 5).stream()
            .map(r -> new TopToy(r.toy().name(), r.score()))
            .toList();
        List<RunSummary> recent = calculations.list().stream().limit(5).toList();

        return new Summary(catalog.toys().size(), catalog.criteria().size(),
            catalog.categories().size(), catalog.profiles().size(), dist, top5, recent,
            calculations.publishStatus());
    }
}
