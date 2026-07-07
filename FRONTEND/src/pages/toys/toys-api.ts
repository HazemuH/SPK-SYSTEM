import { apiClient } from "@/lib/api-client";
import type { PageResponse } from "@/lib/types";

export interface Toy {
  id: number;
  name: string;
  categoryCode: string;
  categoryName: string;
  price: number;
  ageMin: number;
  ageMax: number;
  stock: number;
  active: boolean;
  description: string | null;
  tags: string[];
  scores: Record<string, number>;
}

export interface ToyInput {
  name: string;
  categoryCode: string;
  price: number;
  ageMin: number;
  ageMax: number;
  stock: number;
  active: boolean;
  description?: string;
  tags: string[];
  scores: Record<string, number>;
}

export interface ToyListParams {
  search?: string;
  categoryCode?: string;
  page?: number;
  size?: number;
}

export const toysApi = {
  async list(params: ToyListParams): Promise<PageResponse<Toy>> {
    const { data } = await apiClient.get<PageResponse<Toy>>("/toys", { params });
    return data;
  },
  async create(input: ToyInput): Promise<Toy> {
    const { data } = await apiClient.post<Toy>("/toys", input);
    return data;
  },
  async update(id: number, input: ToyInput): Promise<Toy> {
    const { data } = await apiClient.put<Toy>(`/toys/${id}`, input);
    return data;
  },
  async remove(id: number): Promise<void> {
    await apiClient.delete(`/toys/${id}`);
  },
};
