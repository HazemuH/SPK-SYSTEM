import { cn } from "@/lib/utils";

/**
 * A discrete range slider (0..max) with visible tick dots and a bright, glowing
 * active thumb — so you can see exactly which notch you're on. A transparent
 * native `<input type="range">` sits on top for pointer + keyboard accessibility.
 */
export function Slider({
  value,
  max,
  onChange,
  className,
  "aria-label": ariaLabel,
}: {
  value: number;
  max: number;
  onChange: (v: number) => void;
  className?: string;
  "aria-label"?: string;
}) {
  const pct = max > 0 ? (value / max) * 100 : 0;
  const ticks = Array.from({ length: max + 1 }, (_, i) => i);

  return (
    <div className={cn("relative h-6 w-full select-none", className)}>
      {/* track */}
      <div className="absolute inset-x-0 top-1/2 h-1 -translate-y-1/2 rounded-full bg-muted" />
      {/* filled portion up to the thumb */}
      <div
        className="absolute top-1/2 h-1 -translate-y-1/2 rounded-full bg-primary/40"
        style={{ left: 0, width: `${pct}%` }}
      />
      {/* tick dots */}
      {ticks.map((i) => (
        <span
          key={i}
          className="absolute top-1/2 h-2 w-2 -translate-x-1/2 -translate-y-1/2 rounded-full bg-foreground/20"
          style={{ left: `${(i / max) * 100}%` }}
        />
      ))}
      {/* glowing active thumb */}
      <span
        className="pointer-events-none absolute top-1/2 h-5 w-5 -translate-x-1/2 -translate-y-1/2 rounded-full border-2 border-background bg-primary shadow-lg ring-4 ring-primary/30"
        style={{ left: `${pct}%` }}
      />
      {/* transparent native input for interaction + a11y */}
      <input
        type="range"
        min={0}
        max={max}
        step={1}
        value={value}
        aria-label={ariaLabel}
        onChange={(e) => onChange(Number(e.target.value))}
        className="absolute inset-0 h-full w-full cursor-pointer opacity-0"
      />
    </div>
  );
}
