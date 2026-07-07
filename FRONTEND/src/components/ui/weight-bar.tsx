import { cn } from "@/lib/utils";

/** A horizontal progress/weight bar (0..1 or a percentage via `pct`). */
export function WeightBar({
  pct,
  className,
  barClassName,
}: {
  pct: number;
  className?: string;
  barClassName?: string;
}) {
  return (
    <div className={cn("h-2 flex-1 overflow-hidden rounded-full bg-muted", className)}>
      <div
        className={cn("h-full rounded-full bg-primary transition-all", barClassName)}
        style={{ width: `${Math.max(0, Math.min(100, pct))}%` }}
      />
    </div>
  );
}
