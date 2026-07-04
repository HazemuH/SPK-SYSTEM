import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, type RenderOptions } from "@testing-library/react";
import { type ReactElement, type ReactNode } from "react";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "@/features/auth/auth-context";

/**
 * Render a component wrapped in the app's providers (Query + Router + Auth).
 * Use this instead of RTL's bare `render` for anything using hooks/routing.
 */
export function renderWithProviders(
  ui: ReactElement,
  options: { route?: string } & Omit<RenderOptions, "wrapper"> = {},
) {
  const { route = "/", ...rest } = options;

  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={client}>
        <MemoryRouter initialEntries={[route]}>
          <AuthProvider>{children}</AuthProvider>
        </MemoryRouter>
      </QueryClientProvider>
    );
  }

  return render(ui, { wrapper: Wrapper, ...rest });
}
