package com.spkmainan.criterion;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/** Request/response shapes for admin criterion management (10 fixed criteria). */
public final class CriterionDto {

    private CriterionDto() {}

    /** One of a criterion's five subcriteria, with the AHP local priority it carries. */
    public record Level(int level, String code, String label, double priority) {
        public static Level from(CriterionLevelEntity e) {
            return new Level(e.getLevel(), "S" + e.getLevel(), e.getLabel(), e.getPriority());
        }
    }

    public record Response(Long id, String code, int no, String name, String type,
                           String description, String abbr, boolean active, List<Level> levels) {
        public static Response from(CriterionEntity e, List<CriterionLevelEntity> levels) {
            return new Response(e.getId(), e.getCode(), e.getNo(), e.getName(),
                e.getType().name().toLowerCase(), e.getDescription(), e.getAbbr(), e.isActive(),
                levels.stream().map(Level::from).toList());
        }
    }

    public record UpdateRequest(
            @NotBlank(message = "Nama kriteria wajib diisi") String name,
            String description,
            String abbr,
            Boolean active) {}

    /** Create a new criterion. {@code type} is "benefit" or "cost". */
    public record CreateRequest(
            @NotBlank(message = "Nama kriteria wajib diisi") String name,
            @NotBlank(message = "Tipe kriteria wajib diisi (benefit/cost)") String type,
            String description,
            String abbr) {}
}
