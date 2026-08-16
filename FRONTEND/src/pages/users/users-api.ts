import { apiClient } from "@/lib/api-client";

/** A managed application user (mirrors the backend UserResponse). */
export interface User {
  id: string;
  name: string;
  email: string;
  avatar_url: string | null;
  role: string; // "admin" | "user"
}

export interface CreateUserInput {
  username: string;
  email: string;
  name: string;
  password: string;
  role: string; // "ADMIN" | "USER"
}

export interface UpdateUserInput {
  email: string;
  name: string;
  role: string; // "ADMIN" | "USER"
  password?: string; // optional reset
}

export const usersApi = {
  async list(): Promise<User[]> {
    const { data } = await apiClient.get<User[]>("/users");
    return data;
  },
  async create(input: CreateUserInput): Promise<User> {
    const { data } = await apiClient.post<User>("/users", input);
    return data;
  },
  async update(id: string, input: UpdateUserInput): Promise<User> {
    const { data } = await apiClient.put<User>(`/users/${id}`, input);
    return data;
  },
  async remove(id: string): Promise<void> {
    await apiClient.delete(`/users/${id}`);
  },
};
