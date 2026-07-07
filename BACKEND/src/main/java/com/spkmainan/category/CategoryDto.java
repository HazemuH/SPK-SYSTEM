package com.spkmainan.category;

import jakarta.validation.constraints.NotBlank;

/** Request/response shapes for admin category management. */
public final class CategoryDto {

    private CategoryDto() {}

    public record Response(Long id, String code, String name, String description, long toyCount) {
        public static Response from(CategoryEntity e, long toyCount) {
            return new Response(e.getId(), e.getCode(), e.getName(), e.getDescription(), toyCount);
        }
    }

    public record Request(
            @NotBlank(message = "Nama kategori wajib diisi") String name,
            String description) {}
}
