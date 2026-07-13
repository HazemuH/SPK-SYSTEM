import { useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Calculator,
  Check,
  ChevronLeft,
  ChevronRight,
  Info,
  RotateCcw,
  Table2,
} from "lucide-react";
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

function buildPreset(profile: WeightProfile, criteria: Criterion[]): Record<string, number> {
  const next: Record<string, number> = {};
  for (let i = 0; i < criteria.length; i++) {
    for (let j = i + 1; j < criteria.length; j++) {
      const wi = profile.weights[criteria[i].code] ?? 0.01;
      const wj = profile.weights[criteria[j].code] ?? 0.01;
      next[`${i}-${j}`] = snapToSaaty(wi / wj);
    }
  }
  return next;
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
  const [seededCode, setSeededCode] = useState<string | null>(null);
  const [step, setStep] = useState(0); // which row criterion's page we're on
  const [showMatrix, setShowMatrix] = useState(false);
  const [result, setResult] = useState<WeightProfile | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [computing, setComputing] = useState(false);
  const [savedMsg, setSavedMsg] = useState<string | null>(null);

  // Auto-dismiss the "saved" banner after a few seconds.
  useEffect(() => {
    if (!savedMsg) return;
    const t = setTimeout(() => setSavedMsg(null), 6000);
    return () => clearTimeout(t);
  }, [savedMsg]);

  const profiles = profilesQuery.data;
  const criteria = criteriaQuery.data;

  const code = selectedCode ?? params.get("profile") ?? profiles?.[0]?.code ?? null;
  const profile = useMemo(() => profiles?.find((p) => p.code === code), [profiles, code]);

  // Seed the matrix ONCE per profile selection. Re-seeding on every profile refetch
  // (e.g. after "Hitung") would wipe the user's edits — so gate on the profile code.
  useEffect(() => {
    if (!profile || !criteria) return;
    if (seededCode === profile.code) return;
    setMatrix(buildPreset(profile, criteria));
    setSeededCode(profile.code);
    setStep(0);
    setResult(null);
    setError(null);
    setSavedMsg(null);
  }, [profile, criteria, seededCode]);

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
  const setPair = (i: number, j: number, v: number) => {
    setMatrix((m) => ({ ...m, [`${i}-${j}`]: v }));
    setSavedMsg(null); // edits invalidate the "saved" state
  };

  // Live consistency preview over the current (unsaved) matrix.
  const full = criteria.map((_, i) => criteria.map((_, j) => cellValue(i, j)));
  const live = deriveWeights(full);

  const totalSteps = criteria.length - 1; // last criterion is never a "row"
  const rc = criteria[step];
  const isLastStep = step >= totalSteps - 1;

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
      setSavedMsg(`Bobot untuk profil "${profile!.name}" berhasil disimpan.`);
      // Refresh the cached profiles, but keep the current matrix (do not re-seed).
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
          Bandingkan <strong>satu kriteria terhadap sisanya</strong> per halaman: geser slider ke
          arah yang lebih penting (tengah = sama penting), lalu <strong>Lanjut</strong>. Mulai cepat
          dengan <strong>Terapkan preset profil</strong>. Setelah semua, klik{" "}
          <strong>Hitung Bobot &amp; CR</strong> untuk menyimpan.
        </p>
      </div>

      {/* Sticky action bar. */}
      <div className="sticky top-2 z-10 flex flex-wrap items-center justify-between gap-3 rounded-lg border border-border bg-card/95 p-3 shadow-sm backdrop-blur">
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setMatrix(buildPreset(profile, criteria))}
          >
            <RotateCcw className="h-3.5 w-3.5" />
            Terapkan preset profil
          </Button>
          <Button variant="ghost" size="sm" onClick={() => setShowMatrix((s) => !s)}>
            <Table2 className="h-3.5 w-3.5" />
            {showMatrix ? "Sembunyikan tabel" : "Lihat tabel matriks"}
          </Button>
        </div>
        <div className="flex items-center gap-3">
          <ConsistencyBadge cr={live.cr} consistent={live.consistent} live />
          <Button onClick={handleCompute} disabled={computing}>
            <Calculator className="h-4 w-4" />
            {computing ? "Menyimpan…" : "Hitung Bobot & CR"}
          </Button>
        </div>
      </div>

      {savedMsg && (
        <div className="flex items-center gap-2 rounded-lg border border-success/40 bg-success/10 p-3 text-sm text-success">
          <Check className="h-4 w-4 shrink-0" />
          <p className="font-medium">
            {savedMsg} Bobot hasilnya ada di kartu <strong>Hasil Bobot</strong> di bawah — posisi
            slider tetap sesuai isianmu.
          </p>
        </div>
      )}

      {showMatrix && <MatrixTable criteria={criteria} cellValue={cellValue} />}

      {/* One criterion per page. */}
      <Card>
        <CardHeader className="pb-2">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <CardTitle className="text-base">
              Seberapa penting <span className="text-primary">{rc.name}</span> dibanding kriteria
              lain?
            </CardTitle>
            <span className="text-xs font-medium text-muted-foreground">
              Langkah {step + 1} dari {totalSteps}
            </span>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {criteria.slice(step + 1).map((cc, k) => {
            const j = step + 1 + k;
            const value = matrix[`${step}-${j}`] ?? 1;
            return (
              <ComparisonRow
                key={cc.code}
                rowName={rc.name}
                colName={cc.name}
                value={value}
                onChange={(v) => setPair(step, j, v)}
              />
            );
          })}

          <div className="flex items-center justify-between pt-2">
            <Button
              variant="outline"
              onClick={() => setStep((s) => Math.max(0, s - 1))}
              disabled={step === 0}
            >
              <ChevronLeft className="h-4 w-4" />
              Sebelumnya
            </Button>
            {isLastStep ? (
              <Button onClick={handleCompute} disabled={computing}>
                <Calculator className="h-4 w-4" />
                {computing ? "Menyimpan…" : "Selesai — Hitung & Simpan"}
              </Button>
            ) : (
              <Button onClick={() => setStep((s) => Math.min(totalSteps - 1, s + 1))}>
                Lanjut
                <ChevronRight className="h-4 w-4" />
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      {error && <p className="text-sm text-destructive">{error}</p>}

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

function MatrixTable({
  criteria,
  cellValue,
}: {
  criteria: Criterion[];
  cellValue: (i: number, j: number) => number;
}) {
  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="text-sm text-muted-foreground">
          Matriks perbandingan (hasil isianmu)
        </CardTitle>
      </CardHeader>
      <CardContent className="overflow-x-auto">
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
                {criteria.map((_, j) => {
                  const value = cellValue(i, j);
                  const equal = Math.abs(value - 1) < 1e-9;
                  const strength = Math.min(1, Math.abs(Math.log(value)) / Math.log(9));
                  const tint = equal
                    ? undefined
                    : value > 1
                      ? `rgba(79,70,229,${0.08 + strength * 0.28})`
                      : `rgba(239,68,68,${0.08 + strength * 0.28})`;
                  return (
                    <td
                      key={j}
                      className="border border-border p-1 text-center text-muted-foreground"
                      style={{ background: tint }}
                    >
                      {equal ? "1" : fraction(value)}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </CardContent>
    </Card>
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
