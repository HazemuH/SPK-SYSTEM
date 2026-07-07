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
};
