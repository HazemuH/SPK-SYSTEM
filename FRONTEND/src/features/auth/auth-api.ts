import { apiClient } from "@/lib/api-client";
import type { LoginRequest, LoginResponse, User } from "./types";

/** Talks to the backend auth endpoints. The only place that knows their URLs. */
export const authApi = {
  async login(payload: LoginRequest): Promise<LoginResponse> {
    const { data } = await apiClient.post<LoginResponse>("/auth/login", payload);
    return data;
  },

  async logout(): Promise<void> {
    // Best-effort; stateless JWT means the client just drops the token.
    await apiClient.post("/auth/logout").catch(() => undefined);
  },

  async getProfile(): Promise<User> {
    const { data } = await apiClient.get<User>("/auth/profile");
    return data;
  },

  async updateProfile(input: { name: string; email: string; avatarUrl?: string | null }): Promise<User> {
    const { data } = await apiClient.put<User>("/auth/profile", input);
    return data;
  },

  async changePassword(input: { currentPassword: string; newPassword: string }): Promise<void> {
    await apiClient.post("/auth/change-password", input);
  },
};
