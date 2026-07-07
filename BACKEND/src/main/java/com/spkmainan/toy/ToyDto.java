package com.spkmainan.toy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.Map;
import java.util.Set;

/** Request/response shapes for admin toy management. */
public final class ToyDto {

    private ToyDto() {}

    public record Response(
            Long id, String name, String categoryCode, String categoryName, long price,
            int ageMin, int ageMax, int stock, boolean active, String description,
            Set<String> tags, Map<String, Integer> scores) {}

    public record Request(
            @NotBlank(message = "Nama mainan wajib diisi") String name,
            @NotBlank(message = "Kategori wajib dipilih") String categoryCode,
            @PositiveOrZero(message = "Harga tidak boleh negatif") long price,
            @PositiveOrZero int ageMin,
            @PositiveOrZero int ageMax,
            @PositiveOrZero int stock,
            Boolean active,
            String description,
            Set<String> tags,
            Map<String, Integer> scores) {}

    public record ScoresRequest(
            @NotNull(message = "scores wajib diisi") Map<String, Integer> scores) {}
}
