// Registers @testing-library/jest-dom matchers (e.g. toBeInTheDocument) on
// vitest's `expect`, and cleans up the DOM after each test.
import "@testing-library/jest-dom/vitest";
import { afterEach, vi } from "vitest";
import { cleanup } from "@testing-library/react";

// Deterministic localStorage. Node 25 ships an experimental `localStorage`
// global that lacks a working `getItem`, so we stub a simple in-memory one.
const store = new Map<string, string>();
vi.stubGlobal("localStorage", {
  getItem: (key: string) => store.get(key) ?? null,
  setItem: (key: string, value: string) => void store.set(key, String(value)),
  removeItem: (key: string) => void store.delete(key),
  clear: () => store.clear(),
  key: (index: number) => Array.from(store.keys())[index] ?? null,
  get length() {
    return store.size;
  },
});

afterEach(() => {
  cleanup();
  store.clear();
});
