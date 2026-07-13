import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/features/auth/use-auth";
import { cn } from "@/lib/utils";

const TABS = ["Profil Saya", "Tentang Aplikasi"] as const;

export function SettingsPage() {
  const { user } = useAuth();
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
        <Card className="max-w-xl">
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
            <div className="space-y-2">
              <Label>Nama Lengkap</Label>
              <Input value={user?.name ?? ""} readOnly />
            </div>
            <div className="space-y-2">
              <Label>Email</Label>
              <Input value={user?.email ?? ""} readOnly />
            </div>
            <p className="text-xs text-muted-foreground">
              Pengubahan profil &amp; password akan tersedia pada rilis berikutnya.
            </p>
          </CardContent>
        </Card>
      ) : (
        <Card className="max-w-2xl">
          <CardContent className="space-y-4 py-6">
            <div>
              <p className="text-lg font-bold">KIDORA — Pemilihan Mainan Anak</p>
              <p className="text-sm text-muted-foreground">Versi 1.0.0 · Metode AHP-SAW</p>
            </div>
            <p className="text-sm leading-relaxed text-muted-foreground">
              Sistem Pendukung Keputusan pemilihan mainan anak dengan metode hybrid{" "}
              <strong className="text-foreground">AHP-SAW</strong>: <strong>AHP</strong> untuk
              pembobotan kriteria (pairwise + uji konsistensi CR) dan <strong>SAW</strong> untuk
              sintesis alternatif (normalisasi + weighted sum), dengan{" "}
              <strong className="text-foreground">scenario weights</strong> untuk hasil dinamis.
            </p>
            <div className="flex flex-wrap gap-2">
              {[
                "AHP-SAW",
                "5 Profil Bobot",
                "CR ≤ 0,10",
                "10 Kriteria",
                "8 Kategori",
                "50 Mainan",
              ].map((x) => (
                <Badge key={x} variant="secondary">
                  {x}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
