package com.spkmainan.domain;

/** An AHP criterion. 9 benefit + 1 cost (harga). */
public record Criterion(
        String code,
        int no,
        String name,
        CriterionType type,
        String description,
        String abbr) {
}
