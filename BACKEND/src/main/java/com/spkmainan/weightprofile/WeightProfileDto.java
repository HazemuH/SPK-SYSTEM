package com.spkmainan.weightprofile;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/** Request/response shapes for admin weight-profile management + AHP pairwise. */
public final class WeightProfileDto {

    private WeightProfileDto() {}

    public record Response(
            Long id, String code, String name, String shortName, String icon, String description,
            boolean isDefault, boolean active, double cr, double lambdaMax, double ci,
            boolean consistent, Map<String, Double> weights) {
        public static Response from(WeightProfileEntity e) {
            return new Response(e.getId(), e.getCode(), e.getName(), e.getShortName(), e.getIcon(),
                e.getDescription(), e.isDefaultProfile(), e.isActive(), e.getCr(), e.getLambdaMax(),
                e.getCi(), e.getCr() <= 0.10, e.getWeights());
        }
    }

    public record Request(
            @NotBlank(message = "Nama profil wajib diisi") String name,
            String shortName,
            String icon,
            String description) {}

    /** One upper-triangle Saaty comparison: how much more important rowCode is than colCode. */
    public record PairwiseEntry(String rowCode, String colCode, double value) {}

    /** The pairwise matrix as upper-triangle entries (missing pairs default to 1 = equal). */
    public record PairwiseRequest(List<PairwiseEntry> entries) {}
}
