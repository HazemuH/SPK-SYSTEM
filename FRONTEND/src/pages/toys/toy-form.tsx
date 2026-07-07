import { zodResolver } from "@hookform/resolvers/zod";
import { useQuery } from "@tanstack/react-query";
import { Info } from "lucide-react";
import { useState, type ReactNode } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { SlideOver } from "@/components/ui/slide-over";
import { Toggle } from "@/components/ui/toggle";
import { getApiErrorMessage } from "@/lib/api-client";
import { cn } from "@/lib/utils";
import { categoriesApi } from "@/pages/categories/categories-api";
import { criteriaApi } from "@/pages/criteria/criteria-api";
import { toysApi, type Toy, type ToyInput } from "./toys-api";

const ALL_TAGS = [
  "Kayu",
  "Tanpa Baterai",
  "Baterai",
  "Bestseller",
  "Kreatif",
  "Sensorik",
  "Roleplay",
  "Outdoor",
  "Indoor",
  "Motorik",
  "Edukatif",
  "Hemat",
  "Premium",
  "Logika",
  "Sains",
  "Lembut",
  "Remote",
];

const schema = z.object({
  name: z.string().min(1, "Nama mainan wajib diisi"),
  categoryCode: z.string().min(1, "Kategori wajib dipilih"),
  price: z.coerce.number().min(0, "Harga tidak boleh negatif"),
  ageMin: z.coerce.number().min(0),
  ageMax: z.coerce.number().min(0),
  stock: z.coerce.number().min(0),
  description: z.string().optional(),
});
type FormValues = z.infer<typeof schema>;

export function ToyForm({
  toy,
  onClose,
  onSaved,
}: {
  toy: Toy | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const isEdit = !!toy;
  const [tab, setTab] = useState(0);
  const [active, setActive] = useState(toy?.active ?? true);
  const [tags, setTags] = useState<string[]>(toy?.tags ?? []);
  const [serverError, setServerError] = useState<string | null>(null);

  const categoriesQuery = useQuery({ queryKey: ["categories"], queryFn: categoriesApi.list });
  const criteriaQuery = useQuery({ queryKey: ["criteria"], queryFn: criteriaApi.list });
  const benefits = (criteriaQuery.data ?? []).filter((c) => c.type === "benefit");

  const [scores, setScores] = useState<Record<string, number>>(toy?.scores ?? {});

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: toy?.name ?? "",
      categoryCode: toy?.categoryCode ?? "",
      price: toy?.price ?? 0,
      ageMin: toy?.ageMin ?? 0,
      ageMax: toy?.ageMax ?? 12,
      stock: toy?.stock ?? 0,
      description: toy?.description ?? "",
    },
  });

  const toggleTag = (t: string) =>
    setTags((p) => (p.includes(t) ? p.filter((x) => x !== t) : [...p, t]));

  async function onSubmit(values: FormValues) {
    setServerError(null);
    const input: ToyInput = {
      name: values.name,
      categoryCode: values.categoryCode,
      price: values.price,
      ageMin: values.ageMin,
      ageMax: values.ageMax,
      stock: values.stock,
      active,
      description: values.description,
      tags,
      scores: Object.fromEntries(benefits.map((c) => [c.code, scores[c.code] ?? 3])),
    };
    try {
      if (isEdit) await toysApi.update(toy!.id, input);
      else await toysApi.create(input);
      onSaved();
    } catch (err) {
      setServerError(getApiErrorMessage(err));
    }
  }

  return (
    <SlideOver
      open
      onClose={onClose}
      title={isEdit ? `Edit — ${toy!.name}` : "Tambah Mainan Baru"}
      footer={
        <>
          <Button variant="ghost" className="flex-1" onClick={onClose}>
            Batal
          </Button>
          <Button form="toy-form" type="submit" className="flex-1" disabled={isSubmitting}>
            {isSubmitting ? "Menyimpan..." : isEdit ? "Simpan" : "Tambah"}
          </Button>
        </>
      }
    >
      <div className="mb-5 flex gap-2">
        {["Informasi Umum", "Nilai Kriteria"].map((t, i) => (
          <button
            key={t}
            type="button"
            onClick={() => setTab(i)}
            className={cn(
              "rounded-full border px-3 py-1.5 text-sm font-semibold",
              tab === i
                ? "border-primary bg-primary/10 text-primary"
                : "border-border text-muted-foreground",
            )}
          >
            {t}
          </button>
        ))}
      </div>

      <form id="toy-form" onSubmit={handleSubmit(onSubmit)}>
        <div className={tab === 0 ? "space-y-4" : "hidden"}>
          <Field label="Nama Mainan" error={errors.name?.message}>
            <Input placeholder="cth. Balok Kayu Edukasi" {...register("name")} />
          </Field>
          <Field label="Kategori" error={errors.categoryCode?.message}>
            <Select {...register("categoryCode")}>
              <option value="">Pilih kategori…</option>
              {categoriesQuery.data?.map((c) => (
                <option key={c.id} value={c.code}>
                  {c.name}
                </option>
              ))}
            </Select>
          </Field>
          <Field label="Harga (Rp)" error={errors.price?.message}>
            <Input type="number" {...register("price")} />
          </Field>
          <div className="grid grid-cols-2 gap-3">
            <Field label="Usia Min (th)">
              <Input type="number" {...register("ageMin")} />
            </Field>
            <Field label="Usia Max (th)">
              <Input type="number" {...register("ageMax")} />
            </Field>
          </div>
          <Field label="Stok">
            <Input type="number" {...register("stock")} />
          </Field>
          <Field label="Deskripsi">
            <Input placeholder="deskripsi singkat…" {...register("description")} />
          </Field>
          <div className="flex items-center justify-between">
            <Label>Status Aktif</Label>
            <Toggle checked={active} onChange={setActive} />
          </div>
          <div className="space-y-2">
            <Label>Tag (filter mobile · di luar AHP)</Label>
            <div className="flex flex-wrap gap-1.5">
              {ALL_TAGS.map((t) => (
                <button
                  key={t}
                  type="button"
                  onClick={() => toggleTag(t)}
                  className={cn(
                    "rounded-full border px-2.5 py-1 text-xs font-medium",
                    tags.includes(t)
                      ? "border-primary bg-primary/10 text-primary"
                      : "border-border text-muted-foreground",
                  )}
                >
                  {tags.includes(t) ? "✓ " : ""}
                  {t}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className={tab === 1 ? "space-y-3" : "hidden"}>
          <div className="flex items-start gap-2 rounded-lg border border-warning/30 bg-warning/10 p-3 text-sm">
            <Info className="mt-0.5 h-4 w-4 shrink-0 text-warning" />
            <p>
              Skor 1–5 ini jadi nilai alternatif untuk sintesis SAW (dinormalisasi per kriteria).
              <strong> Harga</strong> (cost) otomatis dari tab Informasi Umum.
            </p>
          </div>
          {benefits.map((c) => {
            const val = scores[c.code] ?? 3;
            return (
              <div key={c.code} className="space-y-1.5">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">{c.name}</span>
                  <span className="text-sm font-bold text-primary">{val}</span>
                </div>
                <div className="flex gap-1.5">
                  {[1, 2, 3, 4, 5].map((n) => (
                    <button
                      key={n}
                      type="button"
                      onClick={() => setScores((s) => ({ ...s, [c.code]: n }))}
                      className={cn(
                        "flex-1 rounded-md border py-1.5 text-sm font-semibold",
                        n === val
                          ? "border-primary bg-primary/10 text-primary"
                          : "border-border text-muted-foreground hover:bg-accent",
                      )}
                    >
                      {n}
                    </button>
                  ))}
                </div>
              </div>
            );
          })}
        </div>

        {serverError && <p className="mt-4 text-sm text-destructive">{serverError}</p>}
      </form>
    </SlideOver>
  );
}

function Field({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return (
    <div className="space-y-2">
      <Label>{label}</Label>
      {children}
      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  );
}
