import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Calculator, Check, RefreshCw, Send, Trophy, Upload, X } from "lucide-react";
import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Stepper } from "@/components/ui/stepper";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { getApiErrorMessage } from "@/lib/api-client";
import { calculationsApi, type RunSummary } from "./calculations-api";

export function CalculationPage() {
  const queryClient = useQueryClient();
  const [run, setRun] = useState<RunSummary | null>(null);

  const precheck = useQuery({ queryKey: ["precheck"], queryFn: calculationsApi.precheck });

  const runMutation = useMutation({
    mutationFn: calculationsApi.run,
    onSuccess: (data) => {
      setRun(data);
      void queryClient.invalidateQueries({ queryKey: ["calculations"] });
    },
  });

  const publishMutation = useMutation({
    mutationFn: (id: number) => calculationsApi.publish(id),
    onSuccess: (data) => {
      setRun(data);
      void queryClient.invalidateQueries({ queryKey: ["calculations"] });
      void queryClient.invalidateQueries({ queryKey: ["dashboard-summary"] });
    },
  });

  const unpublishMutation = useMutation({
    mutationFn: (id: number) => calculationsApi.unpublish(id),
    onSuccess: (data) => {
      setRun(data);
      void queryClient.invalidateQueries({ queryKey: ["calculations"] });
      void queryClient.invalidateQueries({ queryKey: ["dashboard-summary"] });
    },
  });

  const step = run ? 2 : 0;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Kalkulasi &amp; Publikasi</h1>
        <p className="text-sm text-muted-foreground">
          Sintesis SAW per profil → publikasi ke aplikasi mobile.
        </p>
      </div>

      <Card>
        <CardContent className="py-5">
          <Stepper steps={["Pre-check", "Sintesis & Ranking", "Publikasi"]} active={step} />
        </CardContent>
      </Card>

      {!run && (
        <Card>
          <CardContent className="space-y-4 py-5">
            <div>
              <p className="text-base font-semibold">Pre-check Kelengkapan</p>
              <p className="text-sm text-muted-foreground">
                Pastikan semua matriks lengkap &amp; konsisten sebelum kalkulasi.
              </p>
            </div>

            {precheck.isLoading ? (
              <LoadingState />
            ) : precheck.isError ? (
              <ErrorState
                message={getApiErrorMessage(precheck.error)}
                onRetry={() => void precheck.refetch()}
              />
            ) : (
              <>
                <div className="space-y-2">
                  {precheck.data!.items.map((item) => (
                    <div
                      key={item.label}
                      className="flex items-center gap-3 rounded-lg border border-border bg-muted/40 p-3"
                    >
                      <div
                        className={`flex h-7 w-7 items-center justify-center rounded-full ${
                          item.ok
                            ? "bg-success/15 text-success"
                            : "bg-destructive/15 text-destructive"
                        }`}
                      >
                        {item.ok ? <Check className="h-4 w-4" /> : <X className="h-4 w-4" />}
                      </div>
                      <div className="flex-1">
                        <p className="text-sm font-medium">{item.label}</p>
                        <p className="text-xs text-muted-foreground">{item.detail}</p>
                      </div>
                      <Badge variant={item.ok ? "default" : "destructive"}>
                        {item.ok ? "OK" : "Belum"}
                      </Badge>
                    </div>
                  ))}
                </div>
                <div className="flex justify-end">
                  <Button
                    size="lg"
                    disabled={!precheck.data!.allOk || runMutation.isPending}
                    onClick={() => runMutation.mutate()}
                  >
                    <Calculator className="h-4 w-4" />
                    {runMutation.isPending ? "Menghitung…" : "Jalankan Kalkulasi AHP-SAW"}
                  </Button>
                </div>
                {runMutation.isError && (
                  <p className="text-sm text-destructive">
                    {getApiErrorMessage(runMutation.error)}
                  </p>
                )}
              </>
            )}
          </CardContent>
        </Card>
      )}

      {run && (
        <div className="space-y-4">
          <Card>
            <CardContent className="py-5">
              <p className="mb-1 text-base font-semibold">
                Ranking Terhitung — {run.results.length} Profil
              </p>
              <p className="mb-4 text-sm text-muted-foreground">
                Sesi #{run.code} · {run.altCount} alternatif · tiap profil menghasilkan ranking
                berbeda.
              </p>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {run.results.map((r) => (
                  <div key={r.profileCode} className="rounded-lg border border-border p-3">
                    <p className="text-sm font-semibold">{r.profileName}</p>
                    <p className="mt-1 text-[11px] uppercase tracking-wide text-muted-foreground">
                      Rekomendasi #1
                    </p>
                    <p className="flex items-center gap-1 truncate text-sm font-medium">
                      <Trophy className="h-3.5 w-3.5 text-amber" />
                      {r.bestToyName}
                    </p>
                    <div className="mt-1.5">
                      <Badge variant={r.consistent ? "default" : "destructive"}>
                        CR {r.cr.toFixed(3)}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="flex flex-wrap items-center justify-between gap-4 py-5">
              <div className="flex items-center gap-3">
                <div
                  className={`flex h-12 w-12 items-center justify-center rounded-xl ${
                    run.published
                      ? "bg-success text-success-foreground"
                      : "bg-primary text-primary-foreground"
                  }`}
                >
                  {run.published ? <Check className="h-6 w-6" /> : <Send className="h-6 w-6" />}
                </div>
                <div>
                  <p className="font-semibold">
                    {run.published
                      ? "Berhasil Dipublikasikan ✓"
                      : "Publikasikan ke Aplikasi Mobile"}
                  </p>
                  <p className="text-sm text-muted-foreground">
                    {run.published
                      ? "Aplikasi mobile kini membaca ranking & skor SAW terbaru."
                      : "Menulis mainan, kriteria, bobot profil & skor SAW ternormalisasi."}
                  </p>
                </div>
              </div>
              {!run.published ? (
                <Button
                  size="lg"
                  disabled={publishMutation.isPending}
                  onClick={() => publishMutation.mutate(run.id)}
                >
                  <Upload className="h-4 w-4" />
                  {publishMutation.isPending ? "Mempublikasikan…" : "Publikasikan"}
                </Button>
              ) : (
                <Button
                  size="lg"
                  variant="outline"
                  disabled={unpublishMutation.isPending}
                  onClick={() => unpublishMutation.mutate(run.id)}
                >
                  {unpublishMutation.isPending ? "Menarik…" : "Tarik Publikasi"}
                </Button>
              )}
            </CardContent>
          </Card>

          <div className="flex gap-2">
            <Button variant="outline" onClick={() => setRun(null)}>
              <RefreshCw className="h-4 w-4" />
              Kalkulasi Ulang
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
