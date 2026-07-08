import { useCallback, useState } from "react";

export type Theme = "light" | "dark";

const STORAGE_KEY = "spk_theme";

function apply(theme: Theme): void {
  document.documentElement.classList.toggle("dark", theme === "dark");
}

/** Read the stored theme, falling back to the OS preference. */
function resolve(): Theme {
  const stored = localStorage.getItem(STORAGE_KEY) as Theme | null;
  if (stored === "light" || stored === "dark") return stored;
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

/** Apply the initial theme before React renders (call once in main.tsx). */
export function initTheme(): void {
  apply(resolve());
}

/** Theme state + a toggle that persists the choice and updates the `.dark` class. */
export function useTheme() {
  const [theme, setThemeState] = useState<Theme>(resolve);

  const setTheme = useCallback((next: Theme) => {
    localStorage.setItem(STORAGE_KEY, next);
    apply(next);
    setThemeState(next);
  }, []);

  const toggle = useCallback(() => {
    setTheme(theme === "dark" ? "light" : "dark");
  }, [theme, setTheme]);

  return { theme, toggle };
}
