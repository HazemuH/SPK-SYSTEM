import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { BarChart3, Plus, Shuffle, Trash2 } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { z } from "zod";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getApiErrorMessage } from "@/lib/api-client";
import { percent } from "@/lib/format";
import { paths } from "@/routes/paths";
import { criteriaApi } from "@/pages/criteria/criteria-api";
import { weightProfilesApi, type WeightProfile } from "./weight-profiles-api";

const schema = z.object({
  name: z.string().min(1, "Nama profil wajib diisi"),
  description: z.string().optional(),
});
type FormValues = z.infer<typeof schema>;

export function WeightProfilesPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [creating, setCreating] = useState(false);
  const [deleting, setDeleting] = useState<WeightProfile | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const profilesQuery = useQuery({
    queryKey: ["weight-profiles"],
    queryFn: weightProfilesApi.list,
  });
  const criteriaQuery = useQuery({ queryKey: ["criteria"], queryFn: criteriaApi.list });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["weight-profiles"] });

  const removeMutation = useMutation({
    mutationFn: (id: number) => weightProfilesApi.remove(id),
    onSuccess: () => {
      void invalidate();
      setDeleting(null);
    },
    onError: (err) => setDeleteError(getApiErrorMessage(err)),
  });

  const critName = (code: string) => criteriaQuery.data?.find((c) => c.code === code)?.name ?? code;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Profil Bobot</h1>
          <p className="text-sm text-muted-foreground">
            Scenario weights — tiap profil = satu hasil pairwise kriteria tervalidasi.
          </p>
        </div>
        <Button onClick={() => setCreating(true)}>
          <Plus className="h-4 w-4" />
          Tambah Profil
        </Button>
      </div>

      {profilesQuery.isLoading ? (
        <LoadingState />
      ) : profilesQuery.isError ? (
        <ErrorState
          message={getApiErrorMessage(profilesQuery.error)}
          onRetry={() => void profilesQuery.refetch()}
        />
      ) : profilesQuery.data!.length === 0 ? (
        <EmptyState message="Belum ada profil bobot." />
      ) : (
        <div className="space-y-3">
          {profilesQuery.data!.map((p) => {
            const top3 = Object.entries(p.weights)
              .sort((a, b) => b[1] - a[1])
              .slice(0, 3);
            return (
              <Card key={p.id} className="flex flex-wrap items-center justify-between gap-4 p-5">
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <p className="text-base font-semibold">{p.name}</p>
                    {p.isDefault && <Badge variant="secondary">Default</Badge>}
                  </div>
                  <p className="text-sm text-muted-foreground">{p.description}</p>
                  <div className="mt-2 flex flex-wrap gap-1.5">
                    {top3.map(([code, w]) => (
                      <span
                        key={code}
                        className="rounded-full border border-border bg-muted px-2 py-0.5 text-xs text-muted-foreground"
                      >
                        {critName(code)} {percent(w)}
                      </span>
                    ))}
                  </div>
                </div>
                <div className="flex flex-col items-end gap-2">
                  <Badge variant={p.consistent ? "default" : "destructive"}>
                    CR {p.cr.toFixed(3)} · {p.consistent ? "Konsisten" : "Tidak"}
                  </Badge>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={() => navigate(paths.results)}>
                      <BarChart3 className="h-4 w-4" />
                      Ranking
                    </Button>
                    <Button
                      size="sm"
                      onClick={() => navigate(`${paths.pairwise}?profile=${p.code}`)}
                    >
                      <Shuffle className="h-4 w-4" />
                      Pairwise
                    </Button>
                    {!p.isDefault && (
                      <Button
                        variant="outline"
                        size="icon"
                        onClick={() => {
                          setDeleteError(null);
                          setDeleting(p);
                        }}
                      >
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    )}
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {creating && (
        <CreateProfileDialog
          onClose={() => setCreating(false)}
          onCreated={(code) => {
            void invalidate();
            setCreating(false);
            navigate(`${paths.pairwise}?profile=${code}`);
          }}
        />
      )}

      <Dialog
        open={!!deleting}
        onClose={() => setDeleting(null)}
        title="Hapus Profil Bobot?"
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeleting(null)}>
              Batal
            </Button>
            <Button
              variant="destructive"
              disabled={removeMutation.isPending}
              onClick={() => deleting && removeMutation.mutate(deleting.id)}
            >
              Ya, Hapus
            </Button>
          </>
        }
      >
        <p className="text-sm text-muted-foreground">
          Yakin menghapus <span className="font-medium text-foreground">{deleting?.name}</span>?
        </p>
        {deleteError && <p className="mt-3 text-sm text-destructive">{deleteError}</p>}
      </Dialog>
    </div>
  );
}

function CreateProfileDialog({
  onClose,
  onCreated,
}: {
  onClose: () => void;
  onCreated: (code: string) => void;
}) {
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    try {
      const created = await weightProfilesApi.create({
        name: values.name,
        description: values.description,
      });
      onCreated(created.code);
    } catch (err) {
      setServerError(getApiErrorMessage(err));
    }
  }

  return (
    <Dialog
      open
      onClose={onClose}
      title="Tambah Profil Bobot"
      description="Setelah dibuat, isi matriks pairwise untuk menghitung bobot & CR."
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Batal
          </Button>
          <Button form="profile-form" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Menyimpan..." : "Lanjut ke Pairwise"}
          </Button>
        </>
      }
    >
      <form id="profile-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="name">Nama Profil</Label>
          <Input id="name" placeholder="cth. Utamakan Kreativitas" {...register("name")} />
          {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
        </div>
        <div className="space-y-2">
          <Label htmlFor="description">Deskripsi</Label>
          <Input
            id="description"
            placeholder="kapan profil ini dipakai…"
            {...register("description")}
          />
        </div>
        {serverError && <p className="text-sm text-destructive">{serverError}</p>}
      </form>
    </Dialog>
  );
}
