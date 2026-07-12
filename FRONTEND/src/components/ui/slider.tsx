import { cn } from "@/lib/utils";

/**
 * A thin, theme-aware range slider. Discrete steps (0..max), used for the
 * pairwise Saaty scale. Labelled at both ends by the caller.
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
  return (
    <input
      type="range"
      min={0}
      max={max}
      step={1}
      value={value}
      aria-label={ariaLabel}
      onChange={(e) => onChange(Number(e.target.value))}
      className={cn(
        "h-2 w-full cursor-pointer appearance-none rounded-full bg-muted accent-primary",
        className,
      )}
    />
  );
}
