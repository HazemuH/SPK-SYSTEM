package com.spkmainan.ahp;

import com.spkmainan.domain.Criterion;
import com.spkmainan.domain.CriterionType;
import com.spkmainan.domain.Toy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Simple Additive Weighting synthesis: normalize the decision matrix per
 * criterion, then take the weighted sum for a given weight profile.
 *
 * <p>Normalization (values in 0..1): benefit r = x / max(col), cost r = min(col) / x.
 * Columns are computed over the supplied universe (the active toys), so subsets
 * (recommend/catalog/compare) share the same normalization.
 */
@Component
public class SawEngine {

    private record Column(double max, double min) {}

    /** Raw value for a toy under a criterion (price for the cost criterion, else the 1–5 rating). */
    public double rawValue(Toy toy, Criterion c) {
        if (c.type() == CriterionType.COST) {
            return toy.price();
        }
        return toy.scores().getOrDefault(c.code(), 0);
    }

    /** Per-criterion column extremes over the universe. */
    private Map<String, Column> columns(List<Toy> universe, List<Criterion> criteria) {
        Map<String, Column> cols = new LinkedHashMap<>();
        for (Criterion c : criteria) {
            double max = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY;
            for (Toy t : universe) {
                double v = rawValue(t, c);
                max = Math.max(max, v);
                min = Math.min(min, v);
            }
            cols.put(c.code(), new Column(max, min));
        }
        return cols;
    }

    /**
     * Normalized matrix r_ij (toyId → criterionCode → 0..1) over the universe.
     */
    public Map<Integer, Map<String, Double>> normalize(List<Toy> universe, List<Criterion> criteria) {
        Map<String, Column> cols = columns(universe, criteria);
        Map<Integer, Map<String, Double>> norm = new LinkedHashMap<>();
        for (Toy t : universe) {
            Map<String, Double> row = new LinkedHashMap<>();
            for (Criterion c : criteria) {
                Column col = cols.get(c.code());
                double x = rawValue(t, c);
                double r = c.type() == CriterionType.COST
                    ? (x > 0 ? col.min() / x : 0)
                    : (col.max() > 0 ? x / col.max() : 0);
                row.put(c.code(), r);
            }
            norm.put(t.id(), row);
        }
        return norm;
    }

    /** SAW score S_i = Σ (w_j × r_ij) for one normalized row and a weight vector. */
    public double score(Map<String, Double> normalizedRow, Map<String, Double> weights) {
        double s = 0;
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            s += e.getValue() * normalizedRow.getOrDefault(e.getKey(), 0.0);
        }
        return s;
    }
}
