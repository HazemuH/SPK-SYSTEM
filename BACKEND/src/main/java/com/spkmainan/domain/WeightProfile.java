package com.spkmainan.domain;

import java.util.Map;

/**
 * A weight profile (scenario). Each is the validated result of an AHP pairwise
 * over the criteria: a weight vector (Σ=1) plus its consistency metrics.
 */
public record WeightProfile(
        String id,
        String name,
        String shortName,
        String icon,
        String description,
        double cr,
        double lambdaMax,
        double ci,
        Map<String, Double> weights) {

    public boolean consistent() {
        return cr <= 0.10;
    }
}
