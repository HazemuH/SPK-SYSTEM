package com.spkmainan.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Stable, minimal pagination envelope for list endpoints. Prefer this over
 * returning Spring's {@code Page} directly (whose JSON shape is verbose and not
 * contract-stable).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
