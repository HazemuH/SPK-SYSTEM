import { useQuery } from "@tanstack/react-query";
import { Package, SlidersHorizontal, Layers, Scale, type LucideIcon } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import { formatDate } from "@/lib/format";
import { useAuth } from "@/features/auth/use-auth";
import { dashboardApi, type DashboardSummary } from "./dashboard-api";

export function DashboardPage() {
  const { user } = useAuth();
  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["dashboard-summary"],
    queryFn: dashboardApi.summary,
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Halo, {user?.name} 👋</h1>
        <p className="text-sm text-muted-foreground">
          Ringkasan sistem pendukung keputusan · Metode AHP-SAW
        </p>
      </div>

      {isLoading ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState message={getApiErrorMessage(error)} onRetry={() => void refetch()} />
      ) : (
        <DashboardContent data={data!} />
      )}
    </div>
  );
}

const STATS: { key: keyof DashboardSummary; label: string; hint: string; icon: LucideIcon }[] = [
  { key: "totalToys", label: "Total Mainan", hint: "alternatif (rating 1–5)", icon: Package },
  { key: "totalCriteria", label: "Kriteria", hint: "9 benefit · 1 cost", icon: SlidersHorizontal },
  { key: "totalCategories", label: "Kategori", hint: "atribut & filter", icon: Layers },
  { key: "totalProfiles", label: "Profil Bobot", hint: "scenario weights", icon: Scale },
];

function DashboardContent({ data }: { data: DashboardSummary }) {
  const maxCat = Math.max(1, ...data.categoryDistribution.map((c) => c.count));
  const maxScore = Math.max(0.0001, ...data.top5.map((t) => t.score));

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {STATS.map((s) => (
          <Card key={s.key}>
            <CardHeader className="flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">{s.label}</CardTitle>
              <s.icon className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{data[s.key] as number}</div>
              <p className="text-xs text-muted-foreground">{s.hint}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Top 5 — Profil Seimbang</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {data.top5.map((t, i) => (
              <div key={t.name} className="flex items-center gap-3">
                <span className="w-4 text-sm font-bold text-muted-foreground">{i + 1}</span>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">{t.name}</p>
                  <WeightBar pct={(t.score / maxScore) * 100} />
                </div>
                <span className="font-mono text-xs text-muted-foreground">
                  {t.score.toFixed(3)}
                </span>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Distribusi Kategori</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2.5">
            {data.categoryDistribution.map((c) => (
              <div key={c.name} className="flex items-center gap-3">
                <span className="w-32 truncate text-sm text-muted-foreground">{c.name}</span>
                <WeightBar pct={(c.count / maxCat) * 100} barClassName="bg-violet" />
                <span className="w-6 text-right text-sm font-semibold">{c.count}</span>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Riwayat Kalkulasi</CardTitle>
        </CardHeader>
        <CardContent>
          {data.recentSessions.length === 0 ? (
            <EmptyState message="Belum ada sesi kalkulasi." />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Sesi</TableHead>
                  <TableHead>Tanggal</TableHead>
                  <TableHead>Rekomendasi #1 (Seimbang)</TableHead>
                  <TableHead>Alt.</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.recentSessions.map((s) => (
                  <TableRow key={s.id}>
                    <TableCell className="font-medium">#{s.code}</TableCell>
                    <TableCell className="text-muted-foreground">{formatDate(s.runAt)}</TableCell>
                    <TableCell>{s.results[0]?.bestToyName ?? "-"}</TableCell>
                    <TableCell>{s.altCount}</TableCell>
                    <TableCell>
                      <Badge variant={s.published ? "default" : "secondary"}>
                        {s.published ? "Terpublikasi" : "Draft"}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
