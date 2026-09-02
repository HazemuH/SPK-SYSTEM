import { useQuery } from "@tanstack/react-query";
import { Download, Trophy } from "lucide-react";
import { useState } from "react";
import { RadarChart } from "@/components/charts/radar-chart";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Select } from "@/components/ui/select";
import { WeightBar } from "@/components/ui/weight-bar";
import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { getApiErrorMessage } from "@/lib/api-client";
import { downloadCsv, exportFilename, type CsvValue } from "@/lib/export";
import { formatDate, percent } from "@/lib/format";
import { publicApi } from "@/lib/public-api";
import {
  calculationsApi,
  type ProfileDetail,
  type RunDetail,
} from "@/pages/calculation/calculations-api";
import { criteriaApi } from "@/pages/criteria/criteria-api";

export function ResultsPage() {
  const [runId, setRunId] = useState<number | null>(null);
  const [profileCode, setProfileCode] = useState<string | null>(null);

  const listQuery = useQuery({ queryKey: ["calculations"], queryFn: calculationsApi.list });
  const activeRunId = runId ?? listQuery.data?.[0]?.id ?? null;

  const detailQuery = useQuery({
    queryKey: ["calculation", activeRunId],
    queryFn: () => calculationsApi.detail(activeRunId!),
    enabled: activeRunId != null,
  });
  const activeProfileCode = profileCode ?? detailQuery.data?.results[0]?.profileCode ?? "";

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Hasil Kalkulasi</h1>
        <p className="text-sm text-muted-foreground">Ranking global per profil bobot.</p>
      </div>

      {listQuery.isLoading ? (
        <LoadingState />
      ) : listQuery.isError ? (
        <ErrorState
          message={getApiErrorMessage(listQuery.error)}
          onRetry={() => void listQuery.refetch()}
        />
      ) : listQuery.data!.length === 0 ? (
        <EmptyState message="Belum ada sesi kalkulasi. Jalankan kalkulasi dulu." />
      ) : (
        <>
          <div className="flex flex-wrap items-center gap-3">
            <Select
              className="w-56"
              value={activeRunId ?? ""}
              onChange={(e) => {
                setRunId(Number(e.target.value));
                setProfileCode(null);
              }}
            >
              {listQuery.data!.map((s) => (
                <option key={s.id} value={s.id}>
                  Sesi #{s.code} {s.published ? "· terpublikasi" : ""}
                </option>
              ))}
            </Select>
            {detailQuery.data && (
              <div className="ml-auto flex flex-wrap gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => exportProfile(detailQuery.data!, activeProfileCode)}
                >
                  <Download className="h-4 w-4" />
                  Export Profil Ini
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => exportAllProfiles(detailQuery.data!)}
                >
                  <Download className="h-4 w-4" />
                  Export Semua Profil
                </Button>
              </div>
            )}
          </div>

          {detailQuery.isLoading ? (
            <LoadingState />
          ) : detailQuery.isError || !detailQuery.data ? (
            <ErrorState
              message={getApiErrorMessage(detailQuery.error)}
              onRetry={() => void detailQuery.refetch()}
            />
          ) : (
            <RankingView
              detail={detailQuery.data}
              profileCode={activeProfileCode}
              onProfile={setProfileCode}
            />
          )}
        </>
      )}
    </div>
  );
}

function RankingView({
  detail,
  profileCode,
  onProfile,
}: {
  detail: RunDetail;
  profileCode: string;
  onProfile: (code: string) => void;
}) {
  const [selected, setSelected] = useState<{ id: number; name: string } | null>(null);
  const profile = detail.results.find((r) => r.profileCode === profileCode) ?? detail.results[0];
  if (!profile) return <EmptyState message="Tidak ada hasil." />;
  const maxScore = Math.max(0.0001, ...profile.ranking.map((r) => r.finalScore));
  const winner = profile.ranking[0];

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-sm font-medium text-muted-foreground">Profil bobot:</span>
        {detail.results.map((r) => (
          <button
            key={r.profileCode}
            onClick={() => onProfile(r.profileCode)}
            className={`rounded-full border px-3 py-1.5 text-xs font-semibold transition-colors ${
              r.profileCode === profile.profileCode
                ? "border-violet bg-violet/10 text-violet"
                : "border-border text-muted-foreground hover:bg-accent"
            }`}
          >
            {r.profileName}
          </button>
        ))}
        <span className="ml-auto text-xs text-muted-foreground">
          Ganti profil → ranking berubah
        </span>
      </div>

      {winner && (
        <Card>
          <CardContent className="flex items-center gap-4 py-5">
            <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-gradient-to-br from-amber to-success">
              <Trophy className="h-7 w-7 text-white" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
                Rekomendasi Terbaik · {profile.profileName}
              </p>
              <p className="truncate text-xl font-bold">{winner.toyName}</p>
              <p className="text-sm text-muted-foreground">
                Skor Akhir: <strong>{winner.finalScore.toFixed(4)}</strong> · CR {profile.cr.toFixed(3)}
              </p>
            </div>
            <Badge>Rank #1</Badge>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-10">#</TableHead>
                <TableHead>Nama Mainan</TableHead>
                <TableHead>Kategori</TableHead>
                <TableHead>Skor Akhir</TableHead>
                <TableHead className="w-40">Visual</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {profile.ranking.map((r) => (
                <TableRow
                  key={r.toyId}
                  className="cursor-pointer"
                  onClick={() => setSelected({ id: r.toyId, name: r.toyName })}
                >
                  <TableCell className="font-bold">{r.rank}</TableCell>
                  <TableCell className="font-medium">{r.toyName}</TableCell>
                  <TableCell className="text-muted-foreground">{r.categoryName}</TableCell>
                  <TableCell className="font-mono font-semibold">{r.finalScore.toFixed(4)}</TableCell>
                  <TableCell>
                    <WeightBar pct={(r.finalScore / maxScore) * 100} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {selected && (
        <ToyRadarDialog
          toyId={selected.id}
          toyName={selected.name}
          onClose={() => setSelected(null)}
        />
      )}
    </div>
  );
}

function ToyRadarDialog({
  toyId,
  toyName,
  onClose,
}: {
  toyId: number;
  toyName: string;
  onClose: () => void;
}) {
  const detailQuery = useQuery({
    queryKey: ["public-toy", toyId],
    queryFn: () => publicApi.toyDetail(toyId),
  });
  const criteriaQuery = useQuery({ queryKey: ["criteria"], queryFn: criteriaApi.list });

  const isLoading = detailQuery.isLoading || criteriaQuery.isLoading;
  const error = detailQuery.error ?? criteriaQuery.error;
  const criteria = criteriaQuery.data;
  const norm = detailQuery.data?.normalized;

  return (
    <Dialog
      open
      onClose={onClose}
      title={toyName}
      description="Skor ternormalisasi per kriteria (rᵢⱼ)"
    >
      {isLoading ? (
        <LoadingState />
      ) : error || !criteria || !norm ? (
        <ErrorState message={getApiErrorMessage(error)} />
      ) : (
        <div className="space-y-4">
          <div className="flex justify-center">
            <RadarChart
              values={criteria.map((c) => norm[c.code] ?? 0)}
              labels={criteria.map((c) => c.abbr ?? c.name)}
            />
          </div>
          <div className="space-y-2">
            {criteria.map((c) => (
              <div key={c.code} className="flex items-center gap-3">
                <span className="w-28 truncate text-sm text-muted-foreground">{c.name}</span>
                <WeightBar
                  pct={(norm[c.code] ?? 0) * 100}
                  barClassName={c.type === "cost" ? "bg-violet" : undefined}
                />
                <span className="w-10 text-right font-mono text-xs font-bold">
                  {percent(norm[c.code] ?? 0)}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </Dialog>
  );
}

/** Session metadata that heads every exported file. */
function runHeader(detail: RunDetail): CsvValue[][] {
  return [
    ["Hasil Kalkulasi KIDORA"],
    ["Sesi", `#${detail.code}`],
    ["Tanggal", formatDate(detail.runAt)],
    ["Jumlah alternatif", detail.altCount],
    ["Status", detail.published ? "Terpublikasi" : "Draft"],
  ];
}

function rankingRows(profile: ProfileDetail): CsvValue[][] {
  return [
    ["Rank", "Nama Mainan", "Kategori", "Skor Akhir"],
    ...profile.ranking.map((r) => [r.rank, r.toyName, r.categoryName, r.finalScore]),
  ];
}

/** The active profile's full ranking. */
function exportProfile(detail: RunDetail, profileCode: string): void {
  const profile = detail.results.find((r) => r.profileCode === profileCode) ?? detail.results[0];
  if (!profile) return;
  downloadCsv(exportFilename("hasil-sesi", detail.code, profile.profileName), [
    ...runHeader(detail),
    ["Profil bobot", profile.profileName],
    ["CR", profile.cr, profile.consistent ? "konsisten" : "TIDAK konsisten"],
    [],
    ...rankingRows(profile),
  ]);
}

/** Every profile's ranking in one long-format file (a "Profil" column per block). */
function exportAllProfiles(detail: RunDetail): void {
  const rows: CsvValue[][] = [...runHeader(detail), []];
  for (const profile of detail.results) {
    rows.push([`Profil: ${profile.profileName}`]);
    rows.push(["CR", profile.cr, profile.consistent ? "konsisten" : "TIDAK konsisten"]);
    rows.push(...rankingRows(profile));
    rows.push([]);
  }
  downloadCsv(exportFilename("hasil-sesi", detail.code, "semua-profil"), rows);
}
