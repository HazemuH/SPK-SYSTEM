/**
 * Client-side AHP math — a faithful mirror of the backend `AhpEngine`
 * (geometric-mean weights + consistency ratio). Used to show a *live* CR badge
 * while the user drags pairwise sliders; the backend remains the source of truth
 * when "Hitung Bobot & CR" persists the result.
 */

/** Saaty Random Index by matrix order n (index 0 unused). Matches AhpEngine.RI. */
const RI = [0, 0, 0, 0.58, 0.9, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49];

export const CR_THRESHOLD = 0.1;

/** The 9-point reciprocal Saaty scale, ascending (col-favored … equal … row-favored). */
export const saatyScale = [1 / 9, 1 / 7, 1 / 5, 1 / 3, 1, 3, 5, 7, 9];

export interface AhpResult {
  weights: number[];
  lambdaMax: number;
  ci: number;
  cr: number;
  consistent: boolean;
}

/** Derive weights + consistency from a full n×n reciprocal comparison matrix. */
export function deriveWeights(a: number[][]): AhpResult {
  const n = a.length;
  if (n === 0) return { weights: [], lambdaMax: 0, ci: 0, cr: 0, consistent: true };

  // Priority vector via geometric mean of each row, then normalize.
  const w = new Array<number>(n);
  let sum = 0;
  for (let i = 0; i < n; i++) {
    let prod = 1;
    for (let j = 0; j < n; j++) prod *= a[i][j];
    w[i] = Math.pow(prod, 1 / n);
    sum += w[i];
  }
  for (let i = 0; i < n; i++) w[i] /= sum;

  // λmax = average over rows of (A·w)_i / w_i
  let lambdaMax = 0;
  for (let i = 0; i < n; i++) {
    let aw = 0;
    for (let j = 0; j < n; j++) aw += a[i][j] * w[j];
    lambdaMax += aw / w[i];
  }
  lambdaMax /= n;

  const ci = n > 1 ? (lambdaMax - n) / (n - 1) : 0;
  const ri = n < RI.length ? RI[n] : RI[RI.length - 1];
  const cr = ri > 0 ? ci / ri : 0;
  return { weights: w, lambdaMax, ci, cr, consistent: cr <= CR_THRESHOLD + 1e-9 };
}

/** Snap a raw ratio to the nearest allowed Saaty value (nearest on a log scale). */
export function snapToSaaty(d: number): number {
  return saatyScale.reduce((best, opt) =>
    Math.abs(Math.log(opt) - Math.log(d)) < Math.abs(Math.log(best) - Math.log(d)) ? opt : best,
  );
}

/** Human label for how strongly one criterion beats another (magnitude ≥ 1). */
export function intensityLabel(magnitude: number): string {
  const m = Math.round(magnitude);
  if (m >= 9) return "mutlak lebih penting";
  if (m >= 7) return "sangat lebih penting";
  if (m >= 5) return "lebih penting";
  return "sedikit lebih penting";
}

/** A plain-Indonesian sentence describing the comparison of row vs col at `value`. */
export function comparisonSentence(rowName: string, colName: string, value: number): string {
  if (Math.abs(value - 1) < 1e-9) return `${rowName} & ${colName} sama penting`;
  if (value > 1) return `${rowName} ${intensityLabel(value)} dari ${colName}`;
  return `${colName} ${intensityLabel(1 / value)} dari ${rowName}`;
}
