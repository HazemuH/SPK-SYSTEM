import { useQuery } from "@tanstack/react-query";
import { ChevronRight, Trophy } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { getApiErrorMessage } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { paths } from "@/routes/paths";
import { calculationsApi } from "@/pages/calculation/calculations-api";

export function ReportsPage() {
  const navigate = useNavigate();
  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["calculations"],
    queryFn: calculationsApi.list,
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Laporan</h1>
        <p className="text-sm text-muted-foreground">Arsip sesi kalkulasi AHP-SAW.</p>
      </div>

      {isLoading ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState message={getApiErrorMessage(error)} onRetry={() => void refetch()} />
      ) : data!.length === 0 ? (
        <EmptyState message="Belum ada sesi kalkulasi." />
      ) : (
        <div className="space-y-3">
          {data!.map((s) => (
            <Card key={s.id} className="flex flex-wrap items-center justify-between gap-4 p-5">
              <div className="flex items-center gap-4">
                <div className="flex h-16 w-16 flex-col items-center justify-center rounded-xl border border-success/20 bg-success/10">
                  <span className="text-[10px] uppercase text-muted-foreground">Sesi</span>
                  <span className="text-2xl font-bold text-success">{s.code}</span>
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <p className="font-semibold">{formatDate(s.runAt)}</p>
                    <Badge variant={s.published ? "default" : "secondary"}>
                      {s.published ? "Terpublikasi" : "Draft"}
                    </Badge>
                  </div>
                  <p className="flex items-center gap-1 text-sm text-muted-foreground">
                    <Trophy className="h-3.5 w-3.5 text-amber" />
                    <span className="font-medium text-foreground">
                      {s.results[0]?.bestToyName ?? "-"}
                    </span>{" "}
                    (Seimbang)
                  </p>
                  <p className="mt-0.5 text-xs text-muted-foreground">
                    {s.altCount} mainan · {s.results.length} profil
                  </p>
                </div>
              </div>
              <Button variant="outline" onClick={() => navigate(paths.results)}>
                Lihat Ranking
                <ChevronRight className="h-4 w-4" />
              </Button>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
