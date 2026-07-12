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
  const [selectedPair, setSelectedPair] = useState<[number, number]>([0, 1]);
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
    setSelectedPair([0, 1]);
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

  // Live consistency preview over the current (unsaved) matrix.
  const full = criteria.map((_, i) => criteria.map((_, j) => cellValue(i, j)));
  const live = deriveWeights(full);

  const [si, sj] = selectedPair;
  const selValue = matrix[`${si}-${sj}`] ?? 1;

  const setPairValue = (v: number) => setMatrix((m) => ({ ...m, [`${si}-${sj}`]: v }));

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
          <strong>Klik satu sel</strong> di tabel, lalu geser slider di bawah untuk menentukan mana
          yang lebih penting — tanpa perlu paham angka pecahan. Perbandingan{" "}
          <strong>hanya antar kriteria</strong>; kebalikannya otomatis. Alternatif dinilai rating
          1–5 (SAW).
        </p>
      </div>

      {/* ── The pairwise editor: friendly slider + live sentence + live CR ── */}
      <Card>
        <CardContent className="space-y-4 p-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm font-medium text-muted-foreground">
              Membandingkan:{" "}
              <span className="font-semibold text-foreground">{criteria[si].name}</span>
              {" ⟷ "}
              <span className="font-semibold text-foreground">{criteria[sj].name}</span>
            </p>
            <ConsistencyBadge cr={live.cr} consistent={live.consistent} live />
          </div>

          <div className="flex items-center gap-3">
            <span className="w-28 truncate text-right text-xs font-semibold text-destructive">
              {criteria[sj].name} menang
            </span>
            <Slider
              value={indexOfValue(selValue)}
              max={saatyScale.length - 1}
              onChange={(idx) => setPairValue(saatyScale[idx])}
              aria-label={`Bandingkan ${criteria[si].name} dengan ${criteria[sj].name}`}
            />
            <span className="w-28 truncate text-xs font-semibold text-primary">
              {criteria[si].name} menang
            </span>
          </div>

          <p className="text-center text-sm">
            <span className="font-semibold">
              {comparisonSentence(criteria[si].name, criteria[sj].name, selValue)}
            </span>
            <span className="ml-1 text-muted-foreground">({fraction(selValue)})</span>
          </p>

          <div className="flex justify-center">
            <Button variant="outline" size="sm" onClick={() => setMatrix(presetFromWeights)}>
              <RotateCcw className="h-3.5 w-3.5" />
              Terapkan preset profil
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* ── Matrix overview: click a cell to edit it above ── */}
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
                      selected={i === si && j === sj}
                      onSelect={() => setSelectedPair([i, j])}
                    />
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </CardContent>
      </Card>

      <div className="flex items-center justify-end gap-3">
        <ConsistencyBadge cr={live.cr} consistent={live.consistent} live />
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

function MatrixCell({
  value,
  editable,
  selected,
  onSelect,
}: {
  value: number;
  editable: boolean;
  selected: boolean;
  onSelect: () => void;
}) {
  const equal = Math.abs(value - 1) < 1e-9;
  if (!editable) {
    return (
      <td className="border border-border bg-muted/40 p-1 text-center text-muted-foreground">
        {equal ? "1" : fraction(value)}
      </td>
    );
  }
  // Tint by strength (indigo = row favored, red = col favored).
  const strength = Math.min(1, Math.abs(Math.log(value)) / Math.log(9));
  const tint = equal
    ? undefined
    : value > 1
      ? `rgba(79,70,229,${0.08 + strength * 0.28})`
      : `rgba(239,68,68,${0.08 + strength * 0.28})`;
  return (
    <td className="border border-border p-0.5 text-center" style={{ background: tint }}>
      <button
        onClick={onSelect}
        className={cn(
          "w-full rounded px-1 py-1 text-xs font-semibold transition-colors hover:bg-accent",
          selected && "ring-2 ring-primary ring-offset-1 ring-offset-card",
        )}
        title="Klik untuk mengatur pasangan ini"
      >
        {fraction(value)}
      </button>
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
