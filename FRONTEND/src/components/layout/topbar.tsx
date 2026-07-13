import { LogOut, Moon, Sun } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/features/auth/use-auth";
import { useTheme } from "@/features/theme/theme";

export function Topbar() {
  const { user, logout } = useAuth();
  const { theme, toggle } = useTheme();

  return (
    <header className="flex h-16 items-center justify-between border-b border-border bg-card px-6">
      <div className="font-semibold md:hidden">KIDORA</div>
      <div className="ml-auto flex items-center gap-4">
        <Button
          variant="outline"
          size="icon"
          onClick={toggle}
          aria-label={theme === "dark" ? "Mode terang" : "Mode gelap"}
        >
          {theme === "dark" ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        </Button>
        <div className="text-right leading-tight">
          <p className="text-sm font-medium">{user?.name}</p>
          <p className="text-xs capitalize text-muted-foreground">{user?.role}</p>
        </div>
        <Button variant="outline" size="sm" onClick={() => void logout()}>
          <LogOut className="h-4 w-4" />
          Keluar
        </Button>
      </div>
    </header>
  );
}
