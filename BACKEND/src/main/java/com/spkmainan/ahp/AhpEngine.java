package com.spkmainan.ahp;

import org.springframework.stereotype.Component;

/**
 * Analytic Hierarchy Process math: derive criterion weights from a Saaty
 * pairwise-comparison matrix and check consistency (CR ≤ 0.10).
 *
 * <p>Weights use the geometric-mean (approximate eigenvector) method — standard,
 * stable, and matches hand calculations closely.
 */
@Component
public class AhpEngine {

    /** Saaty Random Index by matrix order n (index 0 unused). */
    private static final double[] RI = {0, 0, 0, 0.58, 0.90, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49};

    /** Consistency threshold. */
    public static final double CR_THRESHOLD = 0.10;

    /**
     * @param a full n×n reciprocal comparison matrix (a[i][j] = 1/a[j][i], diagonal 1)
     */
    public AhpResult derive(double[][] a) {
        int n = a.length;
        if (n == 0) {
            return new AhpResult(new double[0], 0, 0, 0, true);
        }

        // Priority vector via geometric mean of each row, then normalize.
        double[] w = new double[n];
        double sum = 0;
        for (int i = 0; i < n; i++) {
            double prod = 1.0;
            for (int j = 0; j < n; j++) {
                prod *= a[i][j];
            }
            w[i] = Math.pow(prod, 1.0 / n);
            sum += w[i];
        }
        for (int i = 0; i < n; i++) {
            w[i] /= sum;
        }

        // λmax = average over rows of (A·w)_i / w_i
        double lambdaMax = 0;
        for (int i = 0; i < n; i++) {
            double aw = 0;
            for (int j = 0; j < n; j++) {
                aw += a[i][j] * w[j];
            }
            lambdaMax += aw / w[i];
        }
        lambdaMax /= n;

        double ci = n > 1 ? (lambdaMax - n) / (n - 1) : 0;
        double ri = n < RI.length ? RI[n] : RI[RI.length - 1];
        double cr = ri > 0 ? ci / ri : 0;

        return new AhpResult(w, lambdaMax, ci, cr, cr <= CR_THRESHOLD + 1e-9);
    }
}
