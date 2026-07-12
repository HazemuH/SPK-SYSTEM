import { describe, expect, it } from "vitest";
import { comparisonSentence, deriveWeights, intensityLabel, saatyScale, snapToSaaty } from "./ahp";

/** Build a full reciprocal matrix from an upper-triangle map { "i-j": value }. */
function fullMatrix(n: number, upper: Record<string, number>): number[][] {
  const a = Array.from({ length: n }, () => Array(n).fill(1));
  for (let i = 0; i < n; i++) {
    for (let j = i + 1; j < n; j++) {
      const v = upper[`${i}-${j}`] ?? 1;
      a[i][j] = v;
      a[j][i] = 1 / v;
    }
  }
  return a;
}

describe("deriveWeights", () => {
  it("gives uniform weights and CR≈0 for the identity matrix", () => {
    const r = deriveWeights(fullMatrix(3, {}));
    expect(r.weights.every((w) => Math.abs(w - 1 / 3) < 1e-9)).toBe(true);
    expect(r.cr).toBeLessThan(1e-9);
    expect(r.consistent).toBe(true);
  });

  it("is consistent (CR ≤ 0.10) for a coherent matrix", () => {
    // Keamanan > Edukasi (3), Keamanan > Harga (5), Edukasi > Harga (3): coherent chain.
    const r = deriveWeights(fullMatrix(3, { "0-1": 3, "0-2": 5, "1-2": 3 }));
    expect(r.weights[0]).toBeGreaterThan(r.weights[1]);
    expect(r.weights[1]).toBeGreaterThan(r.weights[2]);
    expect(r.cr).toBeLessThanOrEqual(0.1);
    expect(r.consistent).toBe(true);
  });

  it("is inconsistent (CR > 0.10) for a max-intensity 3-cycle", () => {
    // A>B(9), B>C(9), C>A(9) — contradictory cycle.
    const r = deriveWeights(fullMatrix(3, { "0-1": 9, "1-2": 9, "0-2": 1 / 9 }));
    expect(r.cr).toBeGreaterThan(0.1);
    expect(r.consistent).toBe(false);
  });
});

describe("saatyScale / snapToSaaty", () => {
  it("is the 9-point reciprocal scale, ascending", () => {
    expect(saatyScale).toEqual([1 / 9, 1 / 7, 1 / 5, 1 / 3, 1, 3, 5, 7, 9]);
  });

  it("snaps a raw ratio to the nearest Saaty value on a log scale", () => {
    expect(snapToSaaty(4)).toBe(5); // 4 is closer to 5 than 3 on a log scale
    expect(snapToSaaty(1)).toBe(1);
    expect(snapToSaaty(0.24)).toBe(1 / 5);
  });
});

describe("comparisonSentence / intensityLabel", () => {
  it("reads row-favored when value > 1", () => {
    expect(comparisonSentence("Keamanan", "Edukasi", 3)).toBe(
      "Keamanan sedikit lebih penting dari Edukasi",
    );
    expect(comparisonSentence("Keamanan", "Edukasi", 9)).toBe(
      "Keamanan mutlak lebih penting dari Edukasi",
    );
  });

  it("reads col-favored when value < 1 (uses the reciprocal magnitude)", () => {
    expect(comparisonSentence("Keamanan", "Edukasi", 1 / 3)).toBe(
      "Edukasi sedikit lebih penting dari Keamanan",
    );
  });

  it("reads equal when value = 1", () => {
    expect(comparisonSentence("Keamanan", "Edukasi", 1)).toBe("Keamanan & Edukasi sama penting");
  });

  it("labels intensity by magnitude", () => {
    expect(intensityLabel(3)).toBe("sedikit lebih penting");
    expect(intensityLabel(5)).toBe("lebih penting");
    expect(intensityLabel(7)).toBe("sangat lebih penting");
    expect(intensityLabel(9)).toBe("mutlak lebih penting");
  });
});
