package com.spkmainan.criterion;

import jakarta.validation.constraints.NotBlank;

/** Request/response shapes for admin criterion management (10 fixed criteria). */
public final class CriterionDto {

    private CriterionDto() {}

    public record Response(Long id, String code, int no, String name, String type,
                           String description, String abbr, boolean active) {
        public static Response from(CriterionEntity e) {
            return new Response(e.getId(), e.getCode(), e.getNo(), e.getName(),
                e.getType().name().toLowerCase(), e.getDescription(), e.getAbbr(), e.isActive());
        }
    }

    public record UpdateRequest(
            @NotBlank(message = "Nama kriteria wajib diisi") String name,
            String description,
            String abbr,
            Boolean active) {}
}
