const rupiahFmt = new Intl.NumberFormat("id-ID", {
  style: "currency",
  currency: "IDR",
  maximumFractionDigits: 0,
});

/** Format a number as Indonesian Rupiah, e.g. 120000 → "Rp 120.000". */
export function rupiah(value: number): string {
  return rupiahFmt.format(value);
}

/** Format a 0..1 weight/score as a percentage, e.g. 0.2 → "20%". */
export function percent(value: number, digits = 0): string {
  return `${(value * 100).toFixed(digits)}%`;
}

/** Short date from an ISO string, e.g. "07 Jul 2026". */
export function formatDate(iso: string | null | undefined): string {
  if (!iso) return "-";
  return new Date(iso).toLocaleDateString("id-ID", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}
