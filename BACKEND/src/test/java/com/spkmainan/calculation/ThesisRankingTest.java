package com.spkmainan.calculation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.spkmainan.ahp.AhpSynthesisEngine;
import com.spkmainan.domain.DomainCatalog;
import com.spkmainan.domain.Toy;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Locks the seeded catalog to Tabel 4.41 of the thesis. The write-up quotes final
 * scores and ranks for A1–A50, so a change to the seed ratings, the criteria
 * weights, or the subcriteria priorities that moves these numbers is a defect —
 * the app and the document have to agree.
 */
@SpringBootTest
class ThesisRankingTest {

    @Autowired
    private DomainCatalog catalog;

    @Autowired
    private AhpSynthesisEngine ahp;

    /** Alternative code → its final score and rank in Tabel 4.41. */
    private record Expected(int toyId, double score, int rank) {}

    private static final List<Expected> TABEL_4_41 = List.of(
        new Expected(5, 0.3303, 1),    // A5  Bus Mainan
        new Expected(16, 0.3190, 3),   // A16 Boneka Hewan
        new Expected(12, 0.3173, 4),   // A12 Robot Remote Control
        new Expected(31, 0.3056, 5),   // A31 Pop It
        new Expected(23, 0.2966, 6),   // A23 Puzzle Anak
        new Expected(1, 0.2905, 10),   // A1  Mobil – mobilan
        new Expected(19, 0.1627, 48),  // A19 Set Dokter
        new Expected(14, 0.1578, 50)); // A14 Boneka Karakter

    @Test
    void seededCatalogReproducesTabel4_41() {
        List<Toy> toys = catalog.activeToys();
        assertThat(toys).hasSize(50);

        Map<String, Double> weights = catalog.profile("balanced").weights();
        Map<Integer, Map<String, Double>> matrix =
            ahp.priorities(toys, catalog.activeCriteria(), catalog.levelPriorities());

        List<Integer> byScore = toys.stream()
            .sorted(Comparator.comparingDouble(
                (Toy t) -> ahp.score(matrix.get(t.id()), weights)).reversed())
            .map(Toy::id)
            .toList();

        for (Expected e : TABEL_4_41) {
            double score = ahp.score(matrix.get(e.toyId()), weights);
            assertThat(score)
                .as("skor akhir A%d", e.toyId())
                .isCloseTo(e.score(), within(0.00005));
            assertThat(byScore.indexOf(e.toyId()) + 1)
                .as("ranking A%d", e.toyId())
                .isEqualTo(e.rank());
        }
    }

    @Test
    void everyAlternativeSitsOnASubcriterionThatCarriesAPriority() {
        var priorities = catalog.levelPriorities();
        var criteria = catalog.activeCriteria();
        var matrix = ahp.priorities(catalog.activeToys(), criteria, priorities);

        for (var row : matrix.values()) {
            for (var c : criteria) {
                assertThat(row.get(c.code()))
                    .as("prioritas %s", c.code())
                    .isIn(java.util.Arrays.stream(priorities.get(c.code())).boxed().toList());
            }
        }
    }
}
