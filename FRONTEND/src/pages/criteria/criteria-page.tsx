import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Info, Pencil, Plus, Trash2 } from "lucide-react";
import { useState, type ReactNode } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { Toggle } from "@/components/ui/toggle";
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
import { criteriaApi, type Criterion } from "./criteria-api";

const PRICE_CODE = "harga";

export function CriteriaPage() {
  const queryClient = useQueryClient();
  const [profileCode, setProfileCode] = useState("balanced");
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<Criterion | null>(null);
  const [deleting, setDeleting] = useState<Criterion | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const criteriaQuery = useQuery({ queryKey: ["criteria"], queryFn: criteriaApi.list });
  const profilesQuery = useQuery({
    queryKey: ["weight-profiles"],
    queryFn: weightProfilesApi.list,
  });

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ["criteria"] });
    void queryClient.invalidateQueries({ queryKey: ["weight-profiles"] });
  };

  const removeMutation = useMutation({
    mutationFn: (id: number) => criteriaApi.remove(id),
    onSuccess: () => {
      invalidate();
      setDeleting(null);
    },
    onError: (err) => setDeleteError(getApiErrorMessage(err)),
  });

  if (criteriaQuery.isLoading || profilesQuery.isLoading)
    return (
      <Shell onAdd={() => setCreating(true)}>
        <LoadingState />
      </Shell>
    );
  if (criteriaQuery.isError || profilesQuery.isError || !criteriaQuery.data || !profilesQuery.data)
    return (
      <Shell onAdd={() => setCreating(true)}>
        <ErrorState
          message={getApiErrorMessage(criteriaQuery.error ?? profilesQuery.error)}
          onRetry={() => {
            void criteriaQuery.refetch();
            void profilesQuery.refetch();
          }}
        />
      </Shell>
    );

  const criteria = criteriaQuery.data;
  const profiles = profilesQuery.data;
  const profile = profiles.find((p) => p.code === profileCode) ?? profiles[0];
  const maxWeight = Math.max(0.0001, ...Object.values(profile.weights));
  const totalPct = Math.round(Object.values(profile.weights).reduce((a, b) => a + b, 0) * 100);

  return (
    <Shell onAdd={() => setCreating(true)}>
      <div className="flex items-start gap-2 rounded-lg border border-info/30 bg-info/10 p-3 text-sm">
        <Info className="mt-0.5 h-4 w-4 shrink-0 text-info" />
        <p>
          Menambah/menghapus kriteria mengubah dimensi perhitungan. Setelah itu,{" "}
          <strong>jalankan ulang Pairwise</strong> (agar bobot dihitung ulang) dan{" "}
          <strong>beri nilai 1–5</strong> kriteria baru pada tiap mainan. Kriteria{" "}
          <strong>Harga</strong> tetap (nilainya = harga jual, tak bisa dihapus).
        </p>
      </div>

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

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Kriteria — bobot untuk {profile.name}</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-8">No</TableHead>
                <TableHead>Kriteria</TableHead>
                <TableHead>Tipe</TableHead>
                <TableHead className="w-40">Bobot</TableHead>
                <TableHead className="w-20">Aksi</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {criteria.map((c) => {
                const w = profile.weights[c.code] ?? 0;
                const isPrice = c.code === PRICE_CODE;
                return (
                  <TableRow key={c.id}>
                    <TableCell className="font-semibold text-muted-foreground">{c.no}</TableCell>
                    <TableCell>
                      <p className="font-medium">
                        {c.name}
                        {!c.active && (
                          <span className="ml-2 text-xs text-muted-foreground">(nonaktif)</span>
                        )}
                      </p>
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
                    <TableCell>
                      <div className="flex gap-1">
                        <Button variant="outline" size="icon" onClick={() => setEditing(c)}>
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="icon"
                          disabled={isPrice}
                          title={isPrice ? "Kriteria Harga tidak bisa dihapus" : undefined}
                          onClick={() => {
                            setDeleteError(null);
                            setDeleting(c);
                          }}
                        >
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
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

      {(creating || editing) && (
        <CriterionDialog
          criterion={editing}
          onClose={() => {
            setCreating(false);
            setEditing(null);
          }}
          onSaved={() => {
            invalidate();
            setCreating(false);
            setEditing(null);
          }}
        />
      )}

      <Dialog
        open={!!deleting}
        onClose={() => setDeleting(null)}
        title="Hapus Kriteria?"
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
          Hapus <span className="font-medium text-foreground">{deleting?.name}</span>? Nilai
          kriteria ini pada semua mainan &amp; bobotnya di semua profil ikut terhapus.
        </p>
        {deleteError && <p className="mt-3 text-sm text-destructive">{deleteError}</p>}
      </Dialog>
    </Shell>
  );
}

const schema = z.object({
  name: z.string().min(1, "Nama kriteria wajib diisi"),
  description: z.string().optional(),
  abbr: z.string().optional(),
});
type FormValues = z.infer<typeof schema>;

function CriterionDialog({
  criterion,
  onClose,
  onSaved,
}: {
  criterion: Criterion | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const isEdit = !!criterion;
  const [type, setType] = useState<"benefit" | "cost">(criterion?.type ?? "benefit");
  const [active, setActive] = useState(criterion?.active ?? true);
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: criterion?.name ?? "",
      description: criterion?.description ?? "",
      abbr: criterion?.abbr ?? "",
    },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    try {
      if (isEdit) {
        await criteriaApi.update(criterion.id, {
          name: values.name,
          description: values.description,
          abbr: values.abbr,
          active,
        });
      } else {
        await criteriaApi.create({
          name: values.name,
          type,
          description: values.description,
          abbr: values.abbr,
        });
      }
      onSaved();
    } catch (err) {
      setServerError(getApiErrorMessage(err));
    }
  }

  return (
    <Dialog
      open
      onClose={onClose}
      title={isEdit ? "Edit Kriteria" : "Tambah Kriteria"}
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Batal
          </Button>
          <Button form="criterion-form" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Menyimpan..." : "Simpan"}
          </Button>
        </>
      }
    >
      <form id="criterion-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="name">Nama Kriteria</Label>
          <Input id="name" placeholder="cth. Ramah Lingkungan" {...register("name")} />
          {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
        </div>
        {!isEdit && (
          <div className="space-y-2">
            <Label htmlFor="type">Tipe</Label>
            <Select
              id="type"
              value={type}
              onChange={(e) => setType(e.target.value as "benefit" | "cost")}
            >
              <option value="benefit">Benefit (makin tinggi makin baik)</option>
              <option value="cost">Cost (makin rendah makin baik)</option>
            </Select>
            <p className="text-xs text-muted-foreground">
              Semua kriteria dinilai 1–5 (kecuali Harga). Tipe menentukan cara normalisasi SAW.
            </p>
          </div>
        )}
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-2">
            <Label htmlFor="abbr">Singkatan</Label>
            <Input id="abbr" placeholder="cth. Eco" {...register("abbr")} />
          </div>
          {isEdit && (
            <div className="space-y-2">
              <Label>Aktif</Label>
              <div className="flex h-10 items-center">
                <Toggle checked={active} onChange={setActive} />
              </div>
            </div>
          )}
        </div>
        <div className="space-y-2">
          <Label htmlFor="description">Deskripsi</Label>
          <Input id="description" placeholder="deskripsi singkat…" {...register("description")} />
        </div>
        {serverError && <p className="text-sm text-destructive">{serverError}</p>}
      </form>
    </Dialog>
  );
}

function Shell({ children, onAdd }: { children: ReactNode; onAdd: () => void }) {
  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Kriteria AHP</h1>
          <p className="text-sm text-muted-foreground">
            Kriteria penilaian · bobot tergantung profil (dari Pairwise).
          </p>
        </div>
        <Button onClick={onAdd}>
          <Plus className="h-4 w-4" />
          Tambah Kriteria
        </Button>
      </div>
      {children}
    </div>
  );
}
