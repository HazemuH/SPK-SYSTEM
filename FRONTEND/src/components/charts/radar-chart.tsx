/**
 * Minimal SVG radar chart (no chart library). `values` are 0..`max` in the same
 * order as `labels`. Theme-aware via CSS custom properties.
 */
export function RadarChart({
  values,
  labels,
  size = 240,
  max = 1,
}: {
  values: number[];
  labels: string[];
  size?: number;
  max?: number;
}) {
  const n = values.length;
  if (n === 0) return null;

  const cx = size / 2;
  const cy = size / 2;
  const r = size / 2 - 34; // leave room for axis labels
  const angle = (i: number) => ((-90 + (360 / n) * i) * Math.PI) / 180;
  const at = (radius: number, i: number): [number, number] => [
    cx + radius * Math.cos(angle(i)),
    cy + radius * Math.sin(angle(i)),
  ];
  const ring = (frac: number) => values.map((_, i) => at(r * frac, i).join(",")).join(" ");

  const dataPts = values.map((v, i) => at(r * Math.min(1, v / max), i));
  const dataPoly = dataPts.map((p) => p.join(",")).join(" ");

  return (
    <svg
      width={size}
      height={size}
      viewBox={`0 0 ${size} ${size}`}
      role="img"
      aria-label="Radar kriteria"
    >
      {[0.25, 0.5, 0.75, 1].map((f) => (
        <polygon key={f} points={ring(f)} fill="none" style={{ stroke: "hsl(var(--border))" }} />
      ))}
      {values.map((_, i) => {
        const [x, y] = at(r, i);
        return (
          <line key={i} x1={cx} y1={cy} x2={x} y2={y} style={{ stroke: "hsl(var(--border))" }} />
        );
      })}
      <polygon
        points={dataPoly}
        style={{ fill: "hsl(var(--primary) / 0.25)", stroke: "hsl(var(--primary))" }}
        strokeWidth={2}
      />
      {dataPts.map((p, i) => (
        <circle key={i} cx={p[0]} cy={p[1]} r={2.5} style={{ fill: "hsl(var(--primary))" }} />
      ))}
      {labels.map((label, i) => {
        const [x, y] = at(r + 16, i);
        return (
          <text
            key={i}
            x={x}
            y={y}
            fontSize={9}
            textAnchor="middle"
            dominantBaseline="middle"
            style={{ fill: "hsl(var(--muted-foreground))" }}
          >
            {label}
          </text>
        );
      })}
    </svg>
  );
}
