import { LayoutDashboard, ToyBrick, type LucideIcon } from "lucide-react";
import { paths } from "@/routes/paths";

export interface NavItem {
  label: string;
  to: string;
  icon: LucideIcon;
}

/** Sidebar navigation. Add an entry when you add a management page. */
export const navItems: NavItem[] = [
  { label: "Dashboard", to: paths.dashboard, icon: LayoutDashboard },
  { label: "Mainan", to: paths.toys, icon: ToyBrick },
  // { label: "Kriteria", to: paths.criteria, icon: SlidersHorizontal },
];
