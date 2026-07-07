import { useQuery } from "@tanstack/react-query";
import { useState, type ReactNode } from "react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { WeightBar } from "@/components/ui/weight-bar";
import { ErrorState, LoadingState } from "@/components/ui/states";
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
import { weightProfilesApi } from "@/pages/weight-profiles/weight-profiles-api";
import { criteriaApi } from "./criteria-api";

export function CriteriaPage() {
  const [profileCode, setProfileCode] = useState("balanced");

  const criteriaQuery = useQuery({ queryKey: ["criteria"], queryFn: criteriaApi.list });
  const profilesQuery = useQuery({
    queryKey: ["weight-profiles"],
    queryFn: weightProfilesApi.list,
  });

  const isLoading = criteriaQuery.isLoading || profilesQuery.isLoading;
  const isError = criteriaQuery.isError || profilesQuery.isError;

  if (isLoading)
    return (
      <PageShell>
        <LoadingState />
      </PageShell>
    );
  if (isError)
    return (
      <PageShell>
        <ErrorState
          message={getApiErrorMessage(criteriaQuery.error ?? profilesQuery.error)}
          onRetry={() => {
            void criteriaQuery.refetch();
            void profilesQuery.refetch();
          }}
        />
      </PageShell>
    );

  const criteria = criteriaQuery.data!;
  const profiles = profilesQuery.data!;
  const profile = profiles.find((p) => p.code === profileCode) ?? profiles[0];
  const maxWeight = Math.max(0.0001, ...Object.values(profile.weights));
  const totalPct = Math.round(Object.values(profile.weights).reduce((a, b) => a + b, 0) * 100);

  return (
    <PageShell>
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-sm font-medium text-muted-foreground">Profil bobot:</span>
        {profiles.map((p) => (
          <button
            key={p.code}
            onClick={() => setProfileCode(p.code)}
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

      <div className="grid gap-4 lg:grid-cols-[1fr_300px]">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Bobot Kriteria — {profile.name}</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-8">No</TableHead>
                  <TableHead>Kriteria</TableHead>
                  <TableHead>Tipe</TableHead>
                  <TableHead className="w-48">Bobot</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {criteria.map((c) => {
                  const w = profile.weights[c.code] ?? 0;
                  return (
                    <TableRow key={c.id}>
                      <TableCell className="font-semibold text-muted-foreground">{c.no}</TableCell>
                      <TableCell>
                        <p className="font-medium">{c.name}</p>
                        <p className="truncate text-xs text-muted-foreground">{c.description}</p>
                      </TableCell>
                      <TableCell>
                        <Badge variant={c.type === "cost" ? "destructive" : "default"}>
                          {c.type}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <WeightBar pct={(w / maxWeight) * 100} />
                          <span className="w-9 text-right font-mono text-xs font-bold">
                            {percent(w)}
                          </span>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
            <p className="mt-3 text-right text-sm font-bold text-primary">Σ {totalPct}%</p>
          </CardContent>
        </Card>

        <Card className="h-fit">
          <CardHeader>
            <CardTitle className="text-base">Consistency Check</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-center">
            <p className="text-xs uppercase tracking-wide text-muted-foreground">
              CR — {profile.name}
            </p>
            <p className="text-5xl font-bold text-success">{profile.cr.toFixed(3)}</p>
            <Badge variant={profile.consistent ? "default" : "destructive"}>
              {profile.consistent ? "Konsisten — CR ≤ 0,10" : "Tidak konsisten"}
            </Badge>
            <div className="space-y-1.5 pt-2 text-left text-sm">
              {[
                ["λmax", profile.lambdaMax.toFixed(2)],
                ["CI", profile.ci.toFixed(3)],
                ["RI (n=10)", "1.49"],
              ].map(([k, v]) => (
                <div key={k} className="flex justify-between">
                  <span className="text-muted-foreground">{k}</span>
                  <span className="font-mono font-semibold">{v}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </PageShell>
  );
}

function PageShell({ children }: { children: ReactNode }) {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Kriteria AHP</h1>
        <p className="text-sm text-muted-foreground">
          10 kriteria · bobot tergantung profil yang dipilih.
        </p>
      </div>
      {children}
    </div>
  );
}
