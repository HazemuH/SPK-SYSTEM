import { useQuery, useQueryClient } from "@tanstack/react-query";
import { Calculator, Info } from "lucide-react";
import { useEffect, useMemo, useState, type ReactNode } from "react";
import { useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { WeightBar } from "@/components/ui/weight-bar";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { getApiErrorMessage } from "@/lib/api-client";
import type { Criterion } from "@/pages/criteria/criteria-api";
import { criteriaApi } from "@/pages/criteria/criteria-api";
import {
  weightProfilesApi,
  type PairwiseEntry,
  type WeightProfile,
} from "@/pages/weight-profiles/weight-profiles-api";

/** Saaty scale options (A more important … equal … B more important). */
const SCALE = [9, 7, 5, 3, 1, 1 / 3, 1 / 5, 1 / 7, 1 / 9];

const fraction = (v: number) => (v >= 1 ? String(Math.round(v)) : `1/${Math.round(1 / v)}`);

/** Snap a raw ratio to the nearest allowed Saaty value (log distance). */
function snap(d: number): number {
  return SCALE.reduce((best, opt) =>
    Math.abs(Math.log(opt) - Math.log(d)) < Math.abs(Math.log(best) - Math.log(d)) ? opt : best,
  );
}

export function PairwisePage() {
  const [params] = useSearchParams();
  const queryClient = useQueryClient();

  const profilesQuery = useQuery({
    queryKey: ["weight-profiles"],
    queryFn: weightProfilesApi.list,
  });
  const criteriaQuery = useQuery({ queryKey: ["criteria"], queryFn: criteriaApi.list });

  const [selectedCode, setSelectedCode] = useState<string | null>(null);
  const [matrix, setMatrix] = useState<Record<string, number>>({}); // "i-j" (i<j) → value
  const [result, setResult] = useState<WeightProfile | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [computing, setComputing] = useState(false);

  const profiles = profilesQuery.data;
  const criteria = criteriaQuery.data;

  const code = selectedCode ?? params.get("profile") ?? profiles?.[0]?.code ?? null;
  const profile = useMemo(() => profiles?.find((p) => p.code === code), [profiles, code]);

  // Seed the editable matrix from the profile's current weights (a coherent starting point).
  useEffect(() => {
    if (!profile || !criteria) return;
    const next: Record<string, number> = {};
    for (let i = 0; i < criteria.length; i++) {
      for (let j = i + 1; j < criteria.length; j++) {
        const wi = profile.weights[criteria[i].code] ?? 0.01;
        const wj = profile.weights[criteria[j].code] ?? 0.01;
        next[`${i}-${j}`] = snap(wi / wj);
      }
    }
    setMatrix(next);
    setResult(null);
    setError(null);
  }, [profile, criteria]);

  if (profilesQuery.isLoading || criteriaQuery.isLoading)
    return (
      <Shell>
        <LoadingState />
      </Shell>
    );
  if (profilesQuery.isError || criteriaQuery.isError || !profiles || !criteria || !profile)
    return (
      <Shell>
        <ErrorState
          message={getApiErrorMessage(profilesQuery.error ?? criteriaQuery.error)}
          onRetry={() => {
            void profilesQuery.refetch();
            void criteriaQuery.refetch();
          }}
        />
      </Shell>
    );

  const cellValue = (i: number, j: number): number => {
    if (i === j) return 1;
    if (i < j) return matrix[`${i}-${j}`] ?? 1;
    return 1 / (matrix[`${j}-${i}`] ?? 1);
  };

  async function handleCompute() {
    if (!criteria) return;
    setComputing(true);
    setError(null);
    const entries: PairwiseEntry[] = [];
    for (let i = 0; i < criteria.length; i++) {
      for (let j = i + 1; j < criteria.length; j++) {
        entries.push({
          rowCode: criteria[i].code,
          colCode: criteria[j].code,
          value: matrix[`${i}-${j}`] ?? 1,
        });
      }
    }
    try {
      const updated = await weightProfilesApi.computePairwise(profile!.id, entries);
      setResult(updated);
      void queryClient.invalidateQueries({ queryKey: ["weight-profiles"] });
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setComputing(false);
    }
  }

  return (
    <Shell>
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-sm font-medium text-muted-foreground">Profil:</span>
        {profiles.map((p) => (
          <button
            key={p.code}
            onClick={() => setSelectedCode(p.code)}
            className={`rounded-full border px-3 py-1.5 text-xs font-semibold transition-colors ${
              p.code === profile.code
                ? "border-primary bg-primary/10 text-primary"
                : "border-border text-muted-foreground hover:bg-accent"
            }`}
          >
            {p.name}
          </button>
        ))}
      </div>

      <div className="flex items-start gap-2 rounded-lg border border-primary/30 bg-primary/5 p-3 text-sm">
        <Info className="mt-0.5 h-4 w-4 shrink-0 text-primary" />
        <p>
          Pairwise <strong>hanya antar kriteria</strong> (skala Saaty 1–9). Isi segitiga atas —
          kebalikannya otomatis. Alternatif TIDAK di-pairwise (dinilai rating 1–5, disintesis SAW).
        </p>
      </div>

      <Card>
        <CardContent className="overflow-x-auto p-3">
          <table className="border-collapse text-xs">
            <thead>
              <tr>
                <th className="sticky left-0 z-10 bg-card p-2" />
                {criteria.map((c) => (
                  <th
                    key={c.code}
                    className="min-w-14 p-1 text-center font-semibold text-muted-foreground"
                    title={c.name}
                  >
                    {c.abbr}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {criteria.map((rc, i) => (
                <tr key={rc.code}>
                  <th className="sticky left-0 z-10 whitespace-nowrap bg-card p-2 text-right font-semibold text-muted-foreground">
                    {rc.abbr}
                  </th>
                  {criteria.map((_, j) => (
                    <MatrixCell
                      key={j}
                      value={cellValue(i, j)}
                      editable={i < j}
                      onChange={(v) => setMatrix((m) => ({ ...m, [`${i}-${j}`]: v }))}
                    />
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <Button size="lg" onClick={handleCompute} disabled={computing}>
          <Calculator className="h-4 w-4" />
          {computing ? "Menghitung…" : "Hitung Bobot & CR"}
        </Button>
      </div>

      {error && <p className="text-sm text-destructive">{error}</p>}

      {result && <PairwiseResult profile={result} criteria={criteria} />}
    </Shell>
  );
}

function MatrixCell({
  value,
  editable,
  onChange,
}: {
  value: number;
  editable: boolean;
  onChange: (v: number) => void;
}) {
  if (!editable) {
    const equal = Math.abs(value - 1) < 1e-9;
    return (
      <td className="border border-border bg-muted/40 p-1 text-center text-muted-foreground">
        {equal ? "1" : fraction(value)}
      </td>
    );
  }
  // Tint by strength (indigo = row favored, red = col favored).
  const strength = Math.min(1, Math.abs(Math.log(value)) / Math.log(9));
  const tint =
    Math.abs(value - 1) < 1e-9
      ? undefined
      : value > 1
        ? `rgba(79,70,229,${0.08 + strength * 0.28})`
        : `rgba(239,68,68,${0.08 + strength * 0.28})`;
  return (
    <td className="border border-border p-0.5 text-center" style={{ background: tint }}>
      <select
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full cursor-pointer bg-transparent text-center text-xs font-semibold outline-none"
      >
        {SCALE.map((v) => (
          <option key={v} value={v}>
            {fraction(v)}
          </option>
        ))}
      </select>
    </td>
  );
}

function PairwiseResult({ profile, criteria }: { profile: WeightProfile; criteria: Criterion[] }) {
  const maxW = Math.max(0.0001, ...Object.values(profile.weights));
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Hasil Bobot — {profile.name}</CardTitle>
      </CardHeader>
      <CardContent className="grid gap-4 lg:grid-cols-[1fr_240px]">
        <div className="space-y-2">
          {criteria.map((c) => {
            const w = profile.weights[c.code] ?? 0;
            return (
              <div key={c.code} className="flex items-center gap-3">
                <span className="w-32 truncate text-sm">{c.name}</span>
                <WeightBar pct={(w / maxW) * 100} />
                <span className="w-14 text-right font-mono text-xs font-bold">{w.toFixed(4)}</span>
              </div>
            );
          })}
        </div>
        <div className="space-y-2">
          <div className="rounded-lg border border-success/30 bg-success/10 p-4 text-center">
            <p className="text-xs uppercase tracking-wide text-muted-foreground">
              Consistency Ratio
            </p>
            <p className="text-4xl font-bold text-success">{profile.cr.toFixed(3)}</p>
            <Badge variant={profile.consistent ? "default" : "destructive"}>
              {profile.consistent ? "Konsisten" : "CR > 0,10 — perbaiki"}
            </Badge>
          </div>
          {[
            ["λmax", profile.lambdaMax.toFixed(3)],
            ["CI", profile.ci.toFixed(3)],
            ["RI", "1.49"],
          ].map(([k, v]) => (
            <div key={k} className="flex justify-between rounded-md bg-muted px-3 py-1.5 text-sm">
              <span className="text-muted-foreground">{k}</span>
              <span className="font-semibold">{v}</span>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}

function Shell({ children }: { children: ReactNode }) {
  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-semibold">Pairwise Comparison</h1>
        <p className="text-sm text-muted-foreground">
          AHP — perbandingan berpasangan antar kriteria (per profil bobot).
        </p>
      </div>
      {children}
    </div>
  );
}
