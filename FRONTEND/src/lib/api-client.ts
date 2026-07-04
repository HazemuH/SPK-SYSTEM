import axios, { AxiosError } from "axios";
import { env } from "@/config/env";
import { tokenStorage } from "@/lib/token-storage";

/**
 * The one configured axios instance. Every feature's api module imports this —
 * never create another axios instance.
 *
 * - Request interceptor attaches the bearer token.
 * - Response interceptor clears the session on 401 and normalizes errors.
 */
export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: { "Content-Type": "application/json" },
  timeout: 30_000,
});

apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Token expired/invalid — drop it and bounce to login.
      tokenStorage.clear();
      if (!window.location.pathname.startsWith("/login")) {
        window.location.assign("/login");
      }
    }
    return Promise.reject(error);
  },
);

/** Shape of the backend's ErrorResponse (see BACKEND/docs/04_API_REFERENCE.md). */
export interface ApiErrorBody {
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
}

/** Extract a human-readable message from any thrown error. */
export function getApiErrorMessage(error: unknown, fallback = "Terjadi kesalahan"): string {
  if (axios.isAxiosError(error)) {
    const body = error.response?.data as ApiErrorBody | undefined;
    return body?.message ?? error.message ?? fallback;
  }
  return fallback;
}
