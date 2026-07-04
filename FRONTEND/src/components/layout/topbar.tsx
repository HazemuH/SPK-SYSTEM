import { LogOut } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/features/auth/use-auth";

export function Topbar() {
  const { user, logout } = useAuth();

  return (
    <header className="flex h-16 items-center justify-between border-b border-border bg-card px-6">
      <div className="md:hidden font-semibold">SPK Mainan</div>
      <div className="ml-auto flex items-center gap-4">
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
