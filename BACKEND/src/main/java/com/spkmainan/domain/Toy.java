package com.spkmainan.domain;

import java.util.List;
import java.util.Map;

/**
 * A toy (alternative). {@code scores} holds the admin's 1–5 rating per benefit
 * criterion (keyed by criterion code); the cost criterion "harga" uses {@code price}.
 */
public record Toy(
        int id,
        String name,
        String categoryId,
        String categoryName,
        long price,
        int ageMin,
        int ageMax,
        List<String> tags,
        int stock,
        boolean active,
        String description,
        Map<String, Integer> scores) {
}
