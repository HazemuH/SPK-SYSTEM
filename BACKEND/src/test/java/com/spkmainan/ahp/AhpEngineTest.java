package com.spkmainan.ahp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AhpEngineTest {

    private final AhpEngine engine = new AhpEngine();

    /** A matrix built as a_ij = w_i/w_j is perfectly consistent → CR≈0, weights recovered. */
    @Test
    void perfectlyConsistentMatrix_recoversWeights_andCrIsZero() {
        double[] w = {0.5, 0.3, 0.2};
        int n = w.length;
        double[][] a = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = w[i] / w[j];
            }
        }

        AhpResult r = engine.derive(a);

        assertThat(r.cr()).isLessThan(1e-6);
        assertThat(r.consistent()).isTrue();
        assertThat(r.lambdaMax()).isCloseTo(n, org.assertj.core.data.Offset.offset(1e-6));
        for (int i = 0; i < n; i++) {
            assertThat(r.weights()[i]).isCloseTo(w[i], org.assertj.core.data.Offset.offset(1e-9));
        }
    }

    /** A classic slightly-inconsistent 3×3 is still within the CR ≤ 0.10 threshold. */
    @Test
    void mildlyInconsistentMatrix_isConsistentWithinThreshold() {
        double[][] a = {
            {1, 2, 4},
            {0.5, 1, 2},
            {0.25, 0.5, 1},
        };

        AhpResult r = engine.derive(a);

        assertThat(r.weights()).hasSize(3);
        assertThat(r.weights()[0]).isGreaterThan(r.weights()[1]).isGreaterThan(0);
        assertThat(r.weights()[1]).isGreaterThan(r.weights()[2]);
        assertThat(r.consistent()).isTrue();
        // weights sum to 1
        assertThat(r.weights()[0] + r.weights()[1] + r.weights()[2])
            .isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-9));
    }
}
