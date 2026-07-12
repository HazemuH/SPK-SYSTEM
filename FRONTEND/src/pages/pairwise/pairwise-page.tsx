import { useQuery, useQueryClient } from "@tanstack/react-query";
import { Calculator, Info, RotateCcw } from "lucide-react";
import { useEffect, useMemo, useState, type ReactNode } from "react";
import { useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Slider } from "@/components/ui/slider";
import { WeightBar } from "@/components/ui/weight-bar";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { getApiErrorMessage } from "@/lib/api-client";
import { cn } from "@/lib/utils";
import { comparisonSentence, deriveWeights, saatyScale, snapToSaaty } from "@/lib/ahp";
import type { Criterion } from "@/pages/criteria/criteria-api";
import { criteriaApi } from "@/pages/criteria/criteria-api";
import {
  weightProfilesApi,
  type PairwiseEntry,
  type WeightProfile,
} from "@/pages/weight-profiles/weight-profiles-api";

const fraction = (v: number) => (v >= 1 ? String(Math.round(v)) : `1/${Math.round(1 / v)}`);
const indexOfValue = (v: number) =>
  Math.max(
    0,
    saatyScale.findIndex((s) => Math.abs(s - v) < 1e-6),
  );

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

  // A coherent starting point derived from the profile's current weights.
  const presetFromWeights = useMemo(() => {
    if (!profile || !criteria) return {};
    const next: Record<string, number> = {};
    for (let i = 0; i < criteria.length; i++) {
      for (let j = i + 1; j < criteria.length; j++) {
        const wi = profile.weights[criteria[i].code] ?? 0.01;
        const wj = profile.weights[criteria[j].code] ?? 0.01;
        next[`${i}-${j}`] = snapToSaaty(wi / wj);
      }
    }
    return next;
  }, [profile, criteria]);

  useEffect(() => {
    setMatrix(presetFromWeights);
    setResult(null);
    setError(null);
  }, [presetFromWeights]);

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
  const setPair = (i: number, j: number, v: number) =>
    setMatrix((m) => ({ ...m, [`${i}-${j}`]: v }));

  // Live consistency preview over the current (unsaved) matrix.
  const full = criteria.map((_, i) => criteria.map((_, j) => cellValue(i, j)));
  const live = deriveWeights(full);

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
            className={cn(
              "rounded-full border px-3 py-1.5 text-xs font-semibold transition-colors",
              p.code === profile.code
                ? "border-primary bg-primary/10 text-primary"
                : "border-border text-muted-foreground hover:bg-accent",
            )}
          >
            {p.name}
          </button>
        ))}
      </div>

      <div className="flex items-start gap-2 rounded-lg border border-info/30 bg-info/10 p-3 text-sm">
        <Info className="mt-0.5 h-4 w-4 shrink-0 text-info" />
        <p>
          Untuk tiap pasangan, <strong>geser slider</strong> ke arah kriteria yang lebih penting —
          tak perlu paham angka pecahan. Tengah = sama penting. Kalimat di bawah slider menjelaskan
          artinya. Kamu bisa mulai dari <strong>Terapkan preset profil</strong> lalu sesuaikan.
        </p>
      </div>

      {/* Sticky action bar: preset + live CR + compute. */}
      <div className="sticky top-2 z-10 flex flex-wrap items-center justify-between gap-3 rounded-lg border border-border bg-card/95 p-3 shadow-sm backdrop-blur">
        <Button variant="outline" size="sm" onClick={() => setMatrix(presetFromWeights)}>
          <RotateCcw className="h-3.5 w-3.5" />
          Terapkan preset profil
        </Button>
        <div className="flex items-center gap-3">
          <ConsistencyBadge cr={live.cr} consistent={live.consistent} live />
          <Button onClick={handleCompute} disabled={computing}>
            <Calculator className="h-4 w-4" />
            {computing ? "Menghitung…" : "Hitung Bobot & CR"}
          </Button>
        </div>
      </div>

      {/* Question list — grouped by the row criterion. */}
      <div className="space-y-5">
        {criteria.slice(0, -1).map((rc, i) => (
          <Card key={rc.code}>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm text-muted-foreground">
                Seberapa penting <span className="text-foreground">{rc.name}</span> dibanding…
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {criteria.slice(i + 1).map((cc, k) => {
                const j = i + 1 + k;
                const value = matrix[`${i}-${j}`] ?? 1;
                return (
                  <ComparisonRow
                    key={cc.code}
                    rowName={rc.name}
                    colName={cc.name}
                    value={value}
                    onChange={(v) => setPair(i, j, v)}
                  />
                );
              })}
            </CardContent>
          </Card>
        ))}
      </div>

      {error && <p className="text-sm text-destructive">{error}</p>}

      {/* Full matrix as a read-only summary (for reference / thesis). */}
      <details className="rounded-lg border border-border bg-card">
        <summary className="cursor-pointer p-3 text-sm font-semibold text-muted-foreground">
          Lihat matriks lengkap (ringkasan)
        </summary>
        <div className="overflow-x-auto p-3 pt-0">
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
                    <SummaryCell key={j} value={cellValue(i, j)} />
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </details>

      {result && <PairwiseResult profile={result} criteria={criteria} />}
    </Shell>
  );
}

function ComparisonRow({
  rowName,
  colName,
  value,
  onChange,
}: {
  rowName: string;
  colName: string;
  value: number;
  onChange: (v: number) => void;
}) {
  const rowFavored = value > 1 + 1e-9;
  const colFavored = value < 1 - 1e-9;
  return (
    <div className="rounded-md border border-border/60 p-3">
      <div className="mb-2 flex items-center justify-between gap-2 text-sm">
        <span className="font-medium">
          vs <span className="font-semibold">{colName}</span>
        </span>
        <span
          className={cn(
            "text-xs font-semibold",
            rowFavored && "text-primary",
            colFavored && "text-destructive",
            !rowFavored && !colFavored && "text-muted-foreground",
          )}
        >
          {comparisonSentence(rowName, colName, value)} ({fraction(value)})
        </span>
      </div>
      <Slider
        value={indexOfValue(value)}
        max={saatyScale.length - 1}
        onChange={(idx) => onChange(saatyScale[idx])}
        aria-label={`Bandingkan ${rowName} dengan ${colName}`}
      />
      <div className="mt-1 flex justify-between text-[11px] text-muted-foreground">
        <span>◀ {colName} lebih penting</span>
        <span>{rowName} lebih penting ▶</span>
      </div>
    </div>
  );
}

function ConsistencyBadge({
  cr,
  consistent,
  live,
}: {
  cr: number;
  consistent: boolean;
  live?: boolean;
}) {
  return (
    <Badge variant={consistent ? "default" : "destructive"}>
      {live ? "Perkiraan: " : ""}
      {consistent ? "Konsisten ✓" : "Belum konsisten"} (CR {cr.toFixed(2)})
    </Badge>
  );
}

function SummaryCell({ value }: { value: number }) {
  const equal = Math.abs(value - 1) < 1e-9;
  const strength = Math.min(1, Math.abs(Math.log(value)) / Math.log(9));
  const tint = equal
    ? undefined
    : value > 1
      ? `rgba(79,70,229,${0.08 + strength * 0.28})`
      : `rgba(239,68,68,${0.08 + strength * 0.28})`;
  return (
    <td
      className="border border-border p-1 text-center text-muted-foreground"
      style={{ background: tint }}
    >
      {equal ? "1" : fraction(value)}
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
