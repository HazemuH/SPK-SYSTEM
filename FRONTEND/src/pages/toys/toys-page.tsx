import { useQuery } from "@tanstack/react-query";
import { Plus, Search } from "lucide-react";
import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
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
import { toysApi } from "./toys-api";

const rupiah = new Intl.NumberFormat("id-ID", {
  style: "currency",
  currency: "IDR",
  maximumFractionDigits: 0,
});

/**
 * Management-table template. Copy this file to build any list/CRUD page:
 * useQuery → handle loading/error/empty → render a Table. Wire mutations
 * (add/edit/delete) with useMutation + queryClient.invalidateQueries.
 */
export function ToysPage() {
  const [search, setSearch] = useState("");

  const { data, isLoading, isError, error, refetch } = useQuery({
    queryKey: ["toys"],
    queryFn: toysApi.list,
  });

  const toys = (data ?? []).filter((t) => t.name.toLowerCase().includes(search.toLowerCase()));

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Mainan</h1>
          <p className="text-sm text-muted-foreground">Kelola daftar mainan (alternatif SPK).</p>
        </div>
        <Button>
          <Plus className="h-4 w-4" />
          Tambah Mainan
        </Button>
      </div>

      <div className="relative max-w-xs">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          className="pl-9"
          placeholder="Cari mainan..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {isLoading ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState message={getApiErrorMessage(error)} onRetry={() => void refetch()} />
      ) : toys.length === 0 ? (
        <EmptyState message="Tidak ada mainan yang cocok." />
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nama</TableHead>
              <TableHead>Kategori</TableHead>
              <TableHead className="text-right">Harga</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {toys.map((toy) => (
              <TableRow key={toy.id}>
                <TableCell className="font-medium">{toy.name}</TableCell>
                <TableCell className="text-muted-foreground">{toy.category}</TableCell>
                <TableCell className="text-right">{rupiah.format(toy.price)}</TableCell>
                <TableCell>
                  <Badge variant={toy.active ? "default" : "secondary"}>
                    {toy.active ? "Aktif" : "Nonaktif"}
                  </Badge>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
