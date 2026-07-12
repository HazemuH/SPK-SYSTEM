package com.spkmainan.calculation;

import java.time.Instant;
import java.util.List;

/** Request/response shapes for the calculation & publish flow. */
public final class CalculationDto {

    private CalculationDto() {}

    /** Whether a published snapshot exists and whether data changed since it was published. */
    public record PublishStatus(boolean published, Instant lastPublishedAt, boolean stale) {}

    public record PrecheckItem(String label, boolean ok, String detail) {}

    public record PrecheckResponse(boolean allOk, List<PrecheckItem> items) {}

    public record ProfileSummary(String profileCode, String profileName, double cr,
                                 boolean consistent, String bestToyName) {}

    public record RunSummary(Long id, String code, Instant runAt, int altCount, boolean published,
                             Instant publishedAt, List<ProfileSummary> results) {}

    public record RankingRow(int rank, int toyId, String toyName, String categoryName, double sawScore) {}

    public record ProfileDetail(String profileCode, String profileName, double cr, double lambdaMax,
                                double ci, boolean consistent, List<RankingRow> ranking) {}

    public record RunDetail(Long id, String code, Instant runAt, int altCount, boolean published,
                            Instant publishedAt, List<ProfileDetail> results) {}
}
