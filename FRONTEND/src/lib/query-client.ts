import { QueryClient } from "@tanstack/react-query";

/** App-wide TanStack Query client. Sensible defaults for an admin dashboard. */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
  },
});
