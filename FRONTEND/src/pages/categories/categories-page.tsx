import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getApiErrorMessage } from "@/lib/api-client";
import { categoriesApi, type Category, type CategoryInput } from "./categories-api";

const schema = z.object({
  name: z.string().min(1, "Nama kategori wajib diisi"),
  description: z.string().optional(),
});
type FormValues = z.infer<typeof schema>;

export function CategoriesPage() {
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState<Category | null>(null);
  const [creating, setCreating] = useState(false);
  const [deleting, setDeleting] = useState<Category | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["categories"],
    queryFn: categoriesApi.list,
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["categories"] });

  const removeMutation = useMutation({
    mutationFn: (id: number) => categoriesApi.remove(id),
    onSuccess: () => {
      void invalidate();
      setDeleting(null);
      setDeleteError(null);
    },
    onError: (err) => setDeleteError(getApiErrorMessage(err)),
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Kategori</h1>
          <p className="text-sm text-muted-foreground">
            Atribut pengelompokan &amp; filter — bukan bagian hirarki AHP.
          </p>
        </div>
        <Button onClick={() => setCreating(true)}>
          <Plus className="h-4 w-4" />
          Tambah Kategori
        </Button>
      </div>

      {isLoading ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState message={getApiErrorMessage(error)} onRetry={() => void refetch()} />
      ) : data!.length === 0 ? (
        <EmptyState message="Belum ada kategori." />
      ) : (
        <div className="grid gap-3 sm:grid-cols-2">
          {data!.map((cat) => (
            <Card key={cat.id} className="flex items-center gap-4 p-4">
              <div className="min-w-0 flex-1">
                <p className="font-semibold">{cat.name}</p>
                <p className="truncate text-sm text-muted-foreground">{cat.description}</p>
              </div>
              <div className="text-center">
                <p className="text-xl font-bold text-primary">{cat.toyCount}</p>
                <p className="text-[10px] text-muted-foreground">mainan</p>
              </div>
              <div className="flex gap-1">
                <Button variant="outline" size="icon" onClick={() => setEditing(cat)}>
                  <Pencil className="h-4 w-4" />
                </Button>
                <Button
                  variant="outline"
                  size="icon"
                  onClick={() => {
                    setDeleteError(null);
                    setDeleting(cat);
                  }}
                >
                  <Trash2 className="h-4 w-4 text-destructive" />
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {(creating || editing) && (
        <CategoryDialog
          category={editing}
          onClose={() => {
            setCreating(false);
            setEditing(null);
          }}
          onSaved={() => {
            void invalidate();
            setCreating(false);
            setEditing(null);
          }}
        />
      )}

      <Dialog
        open={!!deleting}
        onClose={() => setDeleting(null)}
        title="Hapus Kategori?"
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

function CategoryDialog({
  category,
  onClose,
  onSaved,
}: {
  category: Category | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const isEdit = !!category;
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { name: category?.name ?? "", description: category?.description ?? "" },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    const input: CategoryInput = { name: values.name, description: values.description };
    try {
      if (isEdit) await categoriesApi.update(category!.id, input);
      else await categoriesApi.create(input);
      onSaved();
    } catch (err) {
      setServerError(getApiErrorMessage(err));
    }
  }

  return (
    <Dialog
      open
      onClose={onClose}
      title={isEdit ? "Edit Kategori" : "Tambah Kategori"}
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Batal
          </Button>
          <Button form="category-form" type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Menyimpan..." : "Simpan"}
          </Button>
        </>
      }
    >
      <form id="category-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="name">Nama Kategori</Label>
          <Input id="name" {...register("name")} />
          {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
        </div>
        <div className="space-y-2">
          <Label htmlFor="description">Deskripsi</Label>
          <Input id="description" {...register("description")} />
        </div>
        {serverError && <p className="text-sm text-destructive">{serverError}</p>}
      </form>
    </Dialog>
  );
}
