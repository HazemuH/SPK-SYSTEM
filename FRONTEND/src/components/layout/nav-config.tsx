import {
  LayoutDashboard,
  Package,
  Layers,
  SlidersHorizontal,
  Scale,
  Shuffle,
  Calculator,
  Trophy,
  FileText,
  Settings,
  type LucideIcon,
} from "lucide-react";
import { paths } from "@/routes/paths";

export interface NavItem {
  label: string;
  to: string;
  icon: LucideIcon;
}

/** Sidebar navigation — mirrors the design's 10 admin sections. */
export const navItems: NavItem[] = [
  { label: "Dashboard", to: paths.dashboard, icon: LayoutDashboard },
  { label: "Data Mainan", to: paths.toys, icon: Package },
  { label: "Kategori", to: paths.categories, icon: Layers },
  { label: "Kriteria", to: paths.criteria, icon: SlidersHorizontal },
  { label: "Profil Bobot", to: paths.weightProfiles, icon: Scale },
  { label: "Pairwise", to: paths.pairwise, icon: Shuffle },
  { label: "Kalkulasi", to: paths.calculation, icon: Calculator },
  { label: "Hasil", to: paths.results, icon: Trophy },
  { label: "Laporan", to: paths.reports, icon: FileText },
  { label: "Pengaturan", to: paths.settings, icon: Settings },
];
