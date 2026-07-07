import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ChevronLeft, ChevronRight, Pencil, Plus, Search, Trash2 } from "lucide-react";
import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { EmptyState, ErrorState, LoadingState } from "@/components/ui/states";
import { getApiErrorMessage } from "@/lib/api-client";
import { rupiah } from "@/lib/format";
import { categoriesApi } from "@/pages/categories/categories-api";
import { ToyForm } from "./toy-form";
import { toysApi, type Toy } from "./toys-api";

const PAGE_SIZE = 10;

export function ToysPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [categoryCode, setCategoryCode] = useState("");
  const [page, setPage] = useState(0);
  const [formToy, setFormToy] = useState<Toy | null>(null);
  const [creating, setCreating] = useState(false);
  const [deleting, setDeleting] = useState<Toy | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const categoriesQuery = useQuery({ queryKey: ["categories"], queryFn: categoriesApi.list });

  const { data, isLoading, isError, error, refetch, isFetching } = useQuery({
    queryKey: ["toys", { search, categoryCode, page }],
    queryFn: () => toysApi.list({ search, categoryCode, page, size: PAGE_SIZE }),
    placeholderData: keepPreviousData,
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["toys"] });

  const removeMutation = useMutation({
    mutationFn: (id: number) => toysApi.remove(id),
    onSuccess: () => {
      void invalidate();
      setDeleting(null);
    },
    onError: (err) => setDeleteError(getApiErrorMessage(err)),
  });

  const resetTo = (fn: () => void) => {
    fn();
    setPage(0);
  };

  const closeForm = () => {
    setCreating(false);
    setFormToy(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Data Mainan</h1>
          <p className="text-sm text-muted-foreground">
            Alternatif SPK · kategori primer + tag filter (rating 1–5 per kriteria).
          </p>
        </div>
        <Button onClick={() => setCreating(true)}>
          <Plus className="h-4 w-4" />
          Tambah Mainan
        </Button>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <div className="relative max-w-xs flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            className="pl-9"
            placeholder="Cari nama mainan…"
            value={search}
            onChange={(e) => resetTo(() => setSearch(e.target.value))}
          />
        </div>
        <Select
          className="w-48"
          value={categoryCode}
          onChange={(e) => resetTo(() => setCategoryCode(e.target.value))}
        >
          <option value="">Semua Kategori</option>
          {categoriesQuery.data?.map((c) => (
            <option key={c.id} value={c.code}>
              {c.name}
            </option>
          ))}
        </Select>
        {data && (
          <p className="ml-auto text-sm text-muted-foreground">{data.totalElements} mainan</p>
        )}
      </div>

      {isLoading ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState message={getApiErrorMessage(error)} onRetry={() => void refetch()} />
      ) : data!.content.length === 0 ? (
        <EmptyState message="Tidak ada mainan yang cocok." />
      ) : (
        <>
          <div className={isFetching ? "opacity-60 transition-opacity" : ""}>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nama &amp; Tag</TableHead>
                  <TableHead>Kategori</TableHead>
                  <TableHead className="text-right">Harga</TableHead>
                  <TableHead>Usia</TableHead>
                  <TableHead>Stok</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="w-20">Aksi</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data!.content.map((toy) => (
                  <TableRow key={toy.id}>
                    <TableCell>
                      <p className="font-medium">{toy.name}</p>
                      <div className="mt-1 flex flex-wrap gap-1">
                        {toy.tags.slice(0, 3).map((tag) => (
                          <span
                            key={tag}
                            className="rounded-full border border-border bg-muted px-2 py-0.5 text-[10px] text-muted-foreground"
                          >
                            {tag}
                          </span>
                        ))}
                      </div>
                    </TableCell>
                    <TableCell className="text-muted-foreground">{toy.categoryName}</TableCell>
                    <TableCell className="text-right font-mono">{rupiah(toy.price)}</TableCell>
                    <TableCell className="text-muted-foreground">
                      {toy.ageMin}–{toy.ageMax} th
                    </TableCell>
                    <TableCell>
                      {toy.stock === 0 ? (
                        <Badge variant="destructive">Habis</Badge>
                      ) : (
                        <span className={toy.stock < 10 ? "font-semibold text-warning" : ""}>
                          {toy.stock}
                        </span>
                      )}
                    </TableCell>
                    <TableCell>
                      <Badge variant={toy.active ? "default" : "secondary"}>
                        {toy.active ? "Aktif" : "Nonaktif"}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-1">
                        <Button variant="outline" size="icon" onClick={() => setFormToy(toy)}>
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="icon"
                          onClick={() => {
                            setDeleteError(null);
                            setDeleting(toy);
                          }}
                        >
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          <div className="flex items-center justify-between">
            <p className="text-sm text-muted-foreground">
              Halaman {data!.page + 1} dari {data!.totalPages}
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={data!.page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                <ChevronLeft className="h-4 w-4" />
                Sebelumnya
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={data!.last}
                onClick={() => setPage((p) => p + 1)}
              >
                Berikutnya
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </>
      )}

      {(creating || formToy) && (
        <ToyForm
          toy={formToy}
          onClose={closeForm}
          onSaved={() => {
            void invalidate();
            closeForm();
          }}
        />
      )}

      <Dialog
        open={!!deleting}
        onClose={() => setDeleting(null)}
        title="Hapus Mainan?"
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
