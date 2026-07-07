package com.spkmainan.publicapi;

import com.spkmainan.common.exception.ResourceNotFoundException;
import com.spkmainan.publicapi.PublicDto.CompareResult;
import com.spkmainan.publicapi.PublicDto.Meta;
import com.spkmainan.publicapi.PublicDto.ProfileView;
import com.spkmainan.publicapi.PublicDto.RankedToy;
import com.spkmainan.publicapi.PublicDto.Recommendation;
import com.spkmainan.publicapi.PublicDto.RecommendRequest;
import com.spkmainan.publicapi.PublicDto.ToyDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public, no-auth read API for the mobile app. Serves the published AHP-SAW
 * result (rankings + normalized matrix); computes recommend/catalog/compare
 * on top of it. See {@code /public/**} in SecurityConfig.
 */
@RestController
@RequestMapping("/public")
@Tag(name = "Public", description = "Read-only endpoints for the mobile app (no auth)")
public class PublicController {

    private final CatalogService catalog;

    public PublicController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/meta")
    @Operation(summary = "Categories, criteria, sort options and weight profiles")
    public Meta meta() {
        return catalog.meta();
    }

    @GetMapping("/profiles")
    @Operation(summary = "Published weight profiles (for the profile switcher)")
    public List<ProfileView> profiles() {
        return catalog.profiles();
    }

    @GetMapping("/top")
    @Operation(summary = "Top-N recommended toys for a profile")
    public List<RankedToy> top(
            @RequestParam(defaultValue = "balanced") String profile,
            @RequestParam(defaultValue = "5") int limit) {
        return catalog.top(profile, limit);
    }

    @GetMapping("/toys")
    @Operation(summary = "Ranked catalog with optional sort/filter/search")
    public List<RankedToy> toys(
            @RequestParam(defaultValue = "balanced") String profile,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "false") boolean inStock,
            @RequestParam(required = false) String search) {
        return catalog.catalog(profile, sort, categoryId, inStock, search);
    }

    @GetMapping("/toys/{id}")
    @Operation(summary = "Toy detail: score, ranks, normalized r_ij, strengths/weaknesses, next-best")
    public ToyDetail toy(@PathVariable int id) {
        ToyDetail detail = catalog.detail(id);
        if (detail == null) {
            throw new ResourceNotFoundException("Toy not found: " + id);
        }
        return detail;
    }

    @PostMapping("/recommend")
    @Operation(summary = "Recommend toys from preference answers (usia/budget/tujuan/prioritas)")
    public Recommendation recommend(@RequestBody RecommendRequest request) {
        return catalog.recommend(request.usia(), request.budget(), request.tujuan(), request.prioritas());
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare 2–4 toys side by side for a profile")
    public CompareResult compare(
            @RequestParam List<Integer> ids,
            @RequestParam(defaultValue = "balanced") String profile) {
        return catalog.compare(ids, profile);
    }
}
