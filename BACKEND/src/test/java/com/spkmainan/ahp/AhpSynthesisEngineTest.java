package com.spkmainan.ahp;

import static org.assertj.core.api.Assertions.assertThat;

import com.spkmainan.domain.Criterion;
import com.spkmainan.domain.CriterionType;
import com.spkmainan.domain.Toy;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AhpSynthesisEngineTest {

    private final AhpSynthesisEngine ahp = new AhpSynthesisEngine();

    private static final Criterion KEAMANAN =
        new Criterion("keamanan", 1, "Keamanan", CriterionType.BENEFIT, "", "Aman");
    private static final Criterion HARGA =
        new Criterion("harga", 4, "Harga", CriterionType.COST, "", "Harga");

    /** Priorities of the thesis, S1 first. */
    private static final Map<String, double[]> PRIORITIES = Map.of(
        "keamanan", new double[]{0.416, 0.262, 0.161, 0.099, 0.062},
        "harga", new double[]{0.427, 0.274, 0.178, 0.075, 0.045});

    private static Toy toy(int id, int keamanan, long price) {
        return new Toy(id, "T" + id, "edukatif", "Edukatif", price, 1, 5, List.of(), 10, true,
            "", Map.of("keamanan", keamanan));
    }

    @Test
    void mapsRatingsAndPricesOntoSubcriteria() {
        // Rating 5 is the best band (S1), rating 1 the worst (S5).
        assertThat(AhpSynthesisEngine.levelOf(toy(1, 5, 10_000), KEAMANAN)).isEqualTo(1);
        assertThat(AhpSynthesisEngine.levelOf(toy(2, 3, 10_000), KEAMANAN)).isEqualTo(3);
        assertThat(AhpSynthesisEngine.levelOf(toy(3, 1, 10_000), KEAMANAN)).isEqualTo(5);

        // Cheapest price is the best band; the boundaries are inclusive from below.
        assertThat(AhpSynthesisEngine.priceLevel(89_999)).isEqualTo(1);
        assertThat(AhpSynthesisEngine.priceLevel(90_000)).isEqualTo(2);
        assertThat(AhpSynthesisEngine.priceLevel(349_999)).isEqualTo(3);
        assertThat(AhpSynthesisEngine.priceLevel(600_000)).isEqualTo(5);
    }

    @Test
    void takesTheBandPriorityRatherThanNormalizingAgainstOtherAlternatives() {
        Toy best = toy(1, 5, 45_000);
        Toy worst = toy(2, 2, 700_000);

        var matrix = ahp.priorities(List.of(best, worst), List.of(KEAMANAN, HARGA), PRIORITIES);

        assertThat(matrix.get(1)).containsEntry("keamanan", 0.416).containsEntry("harga", 0.427);
        assertThat(matrix.get(2)).containsEntry("keamanan", 0.099).containsEntry("harga", 0.045);
    }

    @Test
    void aSubsetYieldsTheSameValuesAsTheFullSet() {
        Toy a = toy(1, 5, 45_000);
        Toy b = toy(2, 2, 700_000);
        List<Criterion> criteria = List.of(KEAMANAN, HARGA);

        var full = ahp.priorities(List.of(a, b), criteria, PRIORITIES);
        var subset = ahp.priorities(List.of(a), criteria, PRIORITIES);

        assertThat(subset.get(1)).isEqualTo(full.get(1));
    }

    @Test
    void scoresTheWeightedSumOfBandPriorities() {
        var matrix = ahp.priorities(List.of(toy(1, 5, 45_000)), List.of(KEAMANAN, HARGA),
            PRIORITIES);
        Map<String, Double> weights = Map.of("keamanan", 0.199, "harga", 0.115);

        // 0.199 × 0.416 + 0.115 × 0.427
        assertThat(ahp.score(matrix.get(1), weights)).isEqualTo(0.199 * 0.416 + 0.115 * 0.427);
    }
}
