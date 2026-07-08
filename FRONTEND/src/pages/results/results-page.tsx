import { useQuery } from "@tanstack/react-query";
import { Trophy } from "lucide-react";
import { useState } from "react";
import { RadarChart } from "@/components/charts/radar-chart";
import { Badge } from "@/components/ui/badge";
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
import { percent } from "@/lib/format";
import { publicApi } from "@/lib/public-api";
import { calculationsApi } from "@/pages/calculation/calculations-api";
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
              profileCode={profileCode ?? detailQuery.data.results[0]?.profileCode ?? ""}
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
  detail: import("@/pages/calculation/calculations-api").RunDetail;
  profileCode: string;
  onProfile: (code: string) => void;
}) {
  const [selected, setSelected] = useState<{ id: number; name: string } | null>(null);
  const profile = detail.results.find((r) => r.profileCode === profileCode) ?? detail.results[0];
  if (!profile) return <EmptyState message="Tidak ada hasil." />;
  const maxScore = Math.max(0.0001, ...profile.ranking.map((r) => r.sawScore));
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
                Skor SAW: <strong>{winner.sawScore.toFixed(4)}</strong> · CR {profile.cr.toFixed(3)}
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
                <TableHead>Skor SAW</TableHead>
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
                  <TableCell className="font-mono font-semibold">{r.sawScore.toFixed(4)}</TableCell>
                  <TableCell>
                    <WeightBar pct={(r.sawScore / maxScore) * 100} />
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
      description="Skor ternormalisasi SAW per kriteria (rᵢⱼ)"
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
