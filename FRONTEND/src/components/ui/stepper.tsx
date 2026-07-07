import { Check } from "lucide-react";
import { cn } from "@/lib/utils";

/** Horizontal stepper. `active` is the 0-based index of the current step. */
export function Stepper({ steps, active }: { steps: string[]; active: number }) {
  return (
    <div className="flex items-center">
      {steps.map((label, i) => {
        const done = i < active;
        const current = i === active;
        return (
          <div key={label} className="flex flex-1 items-center last:flex-none">
            <div className="flex items-center gap-2">
              <div
                className={cn(
                  "flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-semibold",
                  done && "bg-success text-success-foreground",
                  current && "bg-primary text-primary-foreground",
                  !done && !current && "bg-muted text-muted-foreground",
                )}
              >
                {done ? <Check className="h-4 w-4" /> : i + 1}
              </div>
              <span
                className={cn(
                  "whitespace-nowrap text-sm font-medium",
                  current ? "text-foreground" : "text-muted-foreground",
                )}
              >
                {label}
              </span>
            </div>
            {i < steps.length - 1 && (
              <div className={cn("mx-3 h-0.5 flex-1", done ? "bg-success" : "bg-border")} />
            )}
          </div>
        );
      })}
    </div>
  );
}
