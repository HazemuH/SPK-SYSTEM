import { Hammer } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

/** Temporary page for admin screens not yet built (next FE phase). */
export function PlaceholderPage({ title, description }: { title: string; description: string }) {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">{title}</h1>
        <p className="text-sm text-muted-foreground">{description}</p>
      </div>
      <Card>
        <CardContent className="flex flex-col items-center gap-3 py-16 text-center">
          <Hammer className="h-8 w-8 text-muted-foreground" />
          <p className="font-medium">Segera hadir</p>
          <p className="max-w-sm text-sm text-muted-foreground">
            Halaman ini sedang dibangun pada fase berikutnya. Backend & endpoint-nya sudah siap.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
