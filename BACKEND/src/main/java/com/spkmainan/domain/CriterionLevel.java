package com.spkmainan.domain;

/**
 * One subcriterion of an AHP criterion: the S1–S5 rating band an alternative can
 * fall into, plus the local priority that band carries.
 *
 * <p>{@code level} 1 is always the best band (S1) and 5 the worst (S5); the five
 * priorities of a criterion come from its own pairwise comparison and sum to 1.
 * They are the values that enter the synthesis, which is what makes the method
 * plain AHP rather than a min/max normalization.
 */
public record CriterionLevel(String criterionCode, int level, String label, double priority) {

    /** "S1" … "S5" — the code the thesis tables use. */
    public String code() {
        return "S" + level;
    }
}
