import { ToyBrick, SlidersHorizontal, Trophy, type LucideIcon } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/features/auth/use-auth";

interface Stat {
  label: string;
  value: string;
  icon: LucideIcon;
  hint: string;
}

const stats: Stat[] = [
  { label: "Total Mainan", value: "5", icon: ToyBrick, hint: "alternatif terdaftar" },
  { label: "Kriteria", value: "0", icon: SlidersHorizontal, hint: "belum diatur" },
  { label: "Hasil Ranking", value: "-", icon: Trophy, hint: "jalankan perhitungan SPK" },
];

export function DashboardPage() {
  const { user } = useAuth();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Halo, {user?.name} 👋</h1>
        <p className="text-sm text-muted-foreground">
          Ringkasan sistem pendukung keputusan pemilihan mainan.
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {stats.map((stat) => (
          <Card key={stat.label}>
            <CardHeader className="flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.label}
              </CardTitle>
              <stat.icon className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{stat.value}</div>
              <p className="text-xs text-muted-foreground">{stat.hint}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Mulai dari sini</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          Tentukan <span className="font-medium text-foreground">metode SPK</span> (SAW / WP /
          TOPSIS / AHP), lalu atur <span className="font-medium text-foreground">kriteria</span> dan{" "}
          <span className="font-medium text-foreground">mainan</span> sebelum menjalankan
          perhitungan ranking.
        </CardContent>
      </Card>
    </div>
  );
}
