import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authApi } from "@/features/auth/auth-api";
import { useAuth } from "@/features/auth/use-auth";
import { getApiErrorMessage } from "@/lib/api-client";
import { cn } from "@/lib/utils";

const TABS = ["Profil Saya", "Tentang Aplikasi"] as const;

export function SettingsPage() {
  const [tab, setTab] = useState(0);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Pengaturan</h1>
        <p className="text-sm text-muted-foreground">Kelola akun dan preferensi.</p>
      </div>

      <div className="flex gap-1 border-b border-border">
        {TABS.map((t, i) => (
          <button
            key={t}
            onClick={() => setTab(i)}
            className={cn(
              "-mb-px border-b-2 px-4 py-2 text-sm font-medium",
              tab === i
                ? "border-primary text-foreground"
                : "border-transparent text-muted-foreground hover:text-foreground",
            )}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === 0 ? (
        <div className="grid max-w-4xl gap-6 lg:grid-cols-2">
          <ProfileForm />
          <PasswordForm />
        </div>
      ) : (
        <AboutCard />
      )}
    </div>
  );
}

const profileSchema = z.object({
  name: z.string().min(1, "Nama wajib diisi"),
  email: z.string().email("Format email tidak valid"),
});
type ProfileValues = z.infer<typeof profileSchema>;

function ProfileForm() {
  const { user, updateUser } = useAuth();
  const [status, setStatus] = useState<{ type: "ok" | "err"; msg: string } | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ProfileValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: { name: user?.name ?? "", email: user?.email ?? "" },
  });

  async function onSubmit(values: ProfileValues) {
    setStatus(null);
    try {
      const updated = await authApi.updateProfile({ name: values.name, email: values.email });
      updateUser(updated);
      setStatus({ type: "ok", msg: "Profil berhasil diperbarui." });
    } catch (err) {
      setStatus({ type: "err", msg: getApiErrorMessage(err) });
    }
  }

  return (
    <Card>
      <CardContent className="space-y-4 py-6">
        <div className="flex items-center gap-4">
          <div className="flex h-16 w-16 items-center justify-center rounded-full bg-primary text-xl font-bold text-primary-foreground">
            {user?.name?.slice(0, 2).toUpperCase()}
          </div>
          <div>
            <p className="text-lg font-semibold">{user?.name}</p>
            <Badge className="capitalize">{user?.role}</Badge>
          </div>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Nama Lengkap</Label>
            <Input id="name" {...register("name")} />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input id="email" type="email" {...register("email")} />
            {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
          </div>
          {status && (
            <p className={cn("text-sm", status.type === "ok" ? "text-success" : "text-destructive")}>
              {status.msg}
            </p>
          )}
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Menyimpan..." : "Simpan Profil"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, "Password lama wajib diisi"),
    newPassword: z.string().min(8, "Password baru minimal 8 karakter"),
    confirmPassword: z.string().min(1, "Konfirmasi password wajib diisi"),
  })
  .refine((v) => v.newPassword === v.confirmPassword, {
    message: "Konfirmasi password tidak cocok",
    path: ["confirmPassword"],
  });
type PasswordValues = z.infer<typeof passwordSchema>;

function PasswordForm() {
  const [status, setStatus] = useState<{ type: "ok" | "err"; msg: string } | null>(null);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<PasswordValues>({
    resolver: zodResolver(passwordSchema),
    defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" },
  });

  async function onSubmit(values: PasswordValues) {
    setStatus(null);
    try {
      await authApi.changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      reset();
      setStatus({ type: "ok", msg: "Password berhasil diubah." });
    } catch (err) {
      setStatus({ type: "err", msg: getApiErrorMessage(err) });
    }
  }

  return (
    <Card>
      <CardContent className="space-y-4 py-6">
        <div>
          <p className="text-lg font-semibold">Ubah Password</p>
          <p className="text-sm text-muted-foreground">Minimal 8 karakter.</p>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="currentPassword">Password Lama</Label>
            <Input id="currentPassword" type="password" autoComplete="current-password" {...register("currentPassword")} />
            {errors.currentPassword && (
              <p className="text-xs text-destructive">{errors.currentPassword.message}</p>
            )}
          </div>
          <div className="space-y-2">
            <Label htmlFor="newPassword">Password Baru</Label>
            <Input id="newPassword" type="password" autoComplete="new-password" {...register("newPassword")} />
            {errors.newPassword && <p className="text-xs text-destructive">{errors.newPassword.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="confirmPassword">Konfirmasi Password Baru</Label>
            <Input id="confirmPassword" type="password" autoComplete="new-password" {...register("confirmPassword")} />
            {errors.confirmPassword && (
              <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>
            )}
          </div>
          {status && (
            <p className={cn("text-sm", status.type === "ok" ? "text-success" : "text-destructive")}>
              {status.msg}
            </p>
          )}
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Menyimpan..." : "Ubah Password"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

function AboutCard() {
  return (
    <Card className="max-w-2xl">
      <CardContent className="space-y-4 py-6">
        <div>
          <p className="text-lg font-bold">KIDORA — Pemilihan Mainan Anak</p>
          <p className="text-sm text-muted-foreground">Versi 1.0.0 · Metode AHP</p>
        </div>
        <p className="text-sm leading-relaxed text-muted-foreground">
          Sistem Pendukung Keputusan pemilihan mainan anak dengan metode{" "}
          <strong className="text-foreground">AHP</strong>: pembobotan kriteria lewat perbandingan
          berpasangan + uji konsistensi CR, lalu sintesis alternatif (normalisasi + penjumlahan
          terbobot), dengan{" "}
          <strong className="text-foreground">scenario weights</strong> untuk hasil dinamis.
        </p>
        <div className="flex flex-wrap gap-2">
          {["AHP", "5 Profil Bobot", "CR ≤ 0,10", "10 Kriteria", "8 Kategori", "50 Mainan"].map(
            (x) => (
              <Badge key={x} variant="secondary">
                {x}
              </Badge>
            ),
          )}
        </div>
      </CardContent>
    </Card>
  );
}
