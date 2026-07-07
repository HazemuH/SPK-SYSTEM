package com.spkmainan.publicapi;

import java.util.List;
import java.util.Map;

/**
 * Response/request shapes for the public (mobile) API. Field names match what
 * the Flutter app expects. Grouped as nested records to keep the surface in one place.
 */
public final class PublicDto {

    private PublicDto() {}

    public record ToyView(
            int id, String name, String categoryId, String category, long price,
            int ageMin, int ageMax, int stock, boolean active, List<String> tags) {}

    public record RankedToy(int rank, double score, ToyView toy) {}

    public record CriterionView(
            String code, int no, String name, String type, String description, String abbr) {}

    public record CategoryView(String id, String name, String description, long count) {}

    public record ProfileView(
            String id, String name, String shortName, String icon, String description,
            double cr, boolean consistent, Map<String, Double> weights) {}

    public record SortOption(String id, String label) {}

    public record Meta(
            List<CategoryView> categories, List<CriterionView> criteria,
            List<SortOption> sortOptions, List<ProfileView> profiles) {}

    public record ToyDetail(
            RankedToy ranked, int globalRank, int categoryRank, int categoryTotal,
            Map<String, Double> normalized,
            List<CriterionView> strengths, List<CriterionView> weaknesses,
            ToyView nextBest) {}

    public record CompareCell(int toyId, double value, boolean best) {}

    public record CompareRow(CriterionView criterion, List<CompareCell> cells) {}

    public record CompareTotal(int toyId, double score, boolean winner) {}

    public record CompareResult(
            List<ToyView> toys, List<CompareRow> rows, List<CompareTotal> totals) {}

    public record Recommendation(
            String profileId, String profileName, int baseCount, boolean usedFallback,
            List<RankedToy> primary, List<RankedToy> others) {}

    public record RecommendRequest(String usia, String budget, String tujuan, String prioritas) {}
}
