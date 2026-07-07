import { apiClient } from "@/lib/api-client";

export interface Category {
  id: number;
  code: string;
  name: string;
  description: string | null;
  toyCount: number;
}

export interface CategoryInput {
  name: string;
  description?: string;
}

export const categoriesApi = {
  async list(): Promise<Category[]> {
    const { data } = await apiClient.get<Category[]>("/categories");
    return data;
  },
  async create(input: CategoryInput): Promise<Category> {
    const { data } = await apiClient.post<Category>("/categories", input);
    return data;
  },
  async update(id: number, input: CategoryInput): Promise<Category> {
    const { data } = await apiClient.put<Category>(`/categories/${id}`, input);
    return data;
  },
  async remove(id: number): Promise<void> {
    await apiClient.delete(`/categories/${id}`);
  },
};
