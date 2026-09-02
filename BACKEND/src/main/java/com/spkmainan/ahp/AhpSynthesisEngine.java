package com.spkmainan.ahp;

import com.spkmainan.domain.Criterion;
import com.spkmainan.domain.Toy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * AHP synthesis: place every alternative on one of a criterion's five
 * subcriteria (S1–S5), take that band's local priority, and sum the priorities
 * weighted by the criteria weights.
 *
 * <p>{@code S_i = Σ_j (w_j × p_ij)} where {@code p_ij} is the local priority of
 * the band alternative <i>i</i> falls into on criterion <i>j</i>. Priorities come
 * from each criterion's own pairwise comparison and sum to 1, so nothing is
 * rescaled against the other alternatives — the value of an alternative does not
 * change when the catalog does, and a subset (compare, recommend) yields exactly
 * the numbers the full ranking does.
 */
@Component
public class AhpSynthesisEngine {

    /** The built-in cost criterion whose band comes from the toy's selling price. */
    public static final String PRICE_CRITERION_CODE = "harga";

    /** Every criterion has five subcriteria: S1 (best) … S5 (worst). */
    public static final int LEVELS = 5;

    /**
     * Price bands behind the "harga" subcriteria, cheapest first: S1 below 90 rb,
     * then 90–180 rb, 180–350 rb, 350–600 rb, and S5 from 600 rb up.
     */
    public static int priceLevel(long price) {
        if (price >= 600_000) return 5;
        if (price >= 350_000) return 4;
        if (price >= 180_000) return 3;
        if (price >= 90_000) return 2;
        return 1;
    }

    /**
     * Which subcriterion an alternative falls into for a criterion. "harga" reads
     * the price band; every other criterion maps its 1–5 rating straight onto the
     * bands, best rating (5) to S1.
     */
    public static int levelOf(Toy toy, Criterion c) {
        if (PRICE_CRITERION_CODE.equals(c.code())) {
            return priceLevel(toy.price());
        }
        int rating = toy.scores().getOrDefault(c.code(), 0);
        return Math.max(1, Math.min(LEVELS, LEVELS + 1 - rating));
    }

    /** Raw value shown to the admin: the price for "harga", the 1–5 rating otherwise. */
    public double rawValue(Toy toy, Criterion c) {
        if (PRICE_CRITERION_CODE.equals(c.code())) {
            return toy.price();
        }
        return toy.scores().getOrDefault(c.code(), 0);
    }

    /**
     * The decision matrix p_ij (toyId → criterionCode → local priority).
     *
     * @param priorities criterion code → the five priorities, S1 first
     *                   (see {@code DomainCatalog#levelPriorities()})
     */
    public Map<Integer, Map<String, Double>> priorities(
            List<Toy> toys, List<Criterion> criteria, Map<String, double[]> priorities) {
        Map<Integer, Map<String, Double>> matrix = new LinkedHashMap<>();
        for (Toy t : toys) {
            Map<String, Double> row = new LinkedHashMap<>();
            for (Criterion c : criteria) {
                double[] byLevel = priorities.get(c.code());
                int level = levelOf(t, c);
                row.put(c.code(),
                    byLevel != null && byLevel.length >= level ? byLevel[level - 1] : 0.0);
            }
            matrix.put(t.id(), row);
        }
        return matrix;
    }

    /** Final score S_i = Σ (w_j × p_ij) for one row and a weight vector. */
    public double score(Map<String, Double> row, Map<String, Double> weights) {
        double s = 0;
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            s += e.getValue() * row.getOrDefault(e.getKey(), 0.0);
        }
        return s;
    }
}
