package com.spkmainan.ahp;

import static org.assertj.core.api.Assertions.assertThat;

import com.spkmainan.domain.Criterion;
import com.spkmainan.domain.CriterionType;
import com.spkmainan.domain.Toy;
import java.util.List;
import java.util.Map;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

class SawEngineTest {

    private final SawEngine saw = new SawEngine();

    private Toy toy(int id, int benefit, long price) {
        return new Toy(id, "T" + id, "cat", "Cat", price, 0, 12, List.of(), 5, true, "",
            Map.of("b", benefit));
    }

    @Test
    void normalizesBenefitByMax_andCostByMin() {
        Criterion benefit = new Criterion("b", 1, "Benefit", CriterionType.BENEFIT, "", "b");
        Criterion cost = new Criterion("harga", 2, "Harga", CriterionType.COST, "", "harga");
        Toy a = toy(1, 4, 100); // benefit 4, price 100
        Toy b = toy(2, 2, 50);  // benefit 2, price 50
        List<Criterion> criteria = List.of(benefit, cost);

        var norm = saw.normalize(List.of(a, b), criteria);

        // benefit: x/max → A=4/4=1, B=2/4=0.5
        assertThat(norm.get(1).get("b")).isCloseTo(1.0, Offset.offset(1e-9));
        assertThat(norm.get(2).get("b")).isCloseTo(0.5, Offset.offset(1e-9));
        // cost: min/x → A=50/100=0.5, B=50/50=1
        assertThat(norm.get(1).get("harga")).isCloseTo(0.5, Offset.offset(1e-9));
        assertThat(norm.get(2).get("harga")).isCloseTo(1.0, Offset.offset(1e-9));
    }

    @Test
    void weightedSum_scoresHigherForBetterBenefitToy() {
        Criterion benefit = new Criterion("b", 1, "Benefit", CriterionType.BENEFIT, "", "b");
        Criterion cost = new Criterion("harga", 2, "Harga", CriterionType.COST, "", "harga");
        Toy a = toy(1, 4, 100);
        Toy b = toy(2, 2, 50);
        var norm = saw.normalize(List.of(a, b), List.of(benefit, cost));
        Map<String, Double> weights = Map.of("b", 0.7, "harga", 0.3);

        double sa = saw.score(norm.get(1), weights); // 0.7*1 + 0.3*0.5 = 0.85
        double sb = saw.score(norm.get(2), weights); // 0.7*0.5 + 0.3*1 = 0.65

        assertThat(sa).isCloseTo(0.85, Offset.offset(1e-9));
        assertThat(sb).isCloseTo(0.65, Offset.offset(1e-9));
        assertThat(sa).isGreaterThan(sb);
    }
}
