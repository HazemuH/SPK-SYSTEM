package com.spkmainan.ahp;

/**
 * Output of an AHP pairwise derivation.
 *
 * @param weights   priority vector, Σ=1, in the same order as the input matrix
 * @param lambdaMax principal eigenvalue estimate
 * @param ci        consistency index
 * @param cr        consistency ratio (CI / RI)
 * @param consistent whether CR ≤ 0.10
 */
public record AhpResult(double[] weights, double lambdaMax, double ci, double cr, boolean consistent) {
}
