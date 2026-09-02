/**
 * CSV export helpers.
 *
 * Excel on an Indonesian locale reads ";" as the column separator and "," as the
 * decimal mark, so numbers go out with a comma and the file carries a UTF-8 BOM —
 * otherwise "Rp50.000" and accented names arrive mangled.
 */

export type CsvValue = string | number | boolean | null | undefined;

const SEPARATOR = ";";

function cell(value: CsvValue): string {
  if (value === null || value === undefined) return "";
  if (typeof value === "number") {
    return Number.isFinite(value) ? String(value).replace(".", ",") : "";
  }
  const text = String(value);
  return /[";\r\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
}

/** Turns rows into CSV text. Exported for tests; `downloadCsv` is the usual entry point. */
export function toCsv(rows: CsvValue[][]): string {
  return rows.map((row) => row.map(cell).join(SEPARATOR)).join("\r\n");
}

/** Prompts the browser to save `rows` as `<filename>.csv`. */
export function downloadCsv(filename: string, rows: CsvValue[][]): void {
  const blob = new Blob([`﻿${toCsv(rows)}`], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename.endsWith(".csv") ? filename : `${filename}.csv`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

/** `hasil-sesi-001-seimbang-2026-09-02` — safe on every filesystem. */
export function exportFilename(...parts: string[]): string {
  const stamp = new Date().toISOString().slice(0, 10);
  return [...parts, stamp]
    .join("-")
    .toLowerCase()
    .replace(/[^a-z0-9-]+/g, "-")
    .replace(/-+/g, "-")
    .replace(/^-|-$/g, "");
}
