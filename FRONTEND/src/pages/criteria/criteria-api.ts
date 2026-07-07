import { apiClient } from "@/lib/api-client";

export interface Criterion {
  id: number;
  code: string;
  no: number;
  name: string;
  type: "benefit" | "cost";
  description: string | null;
  abbr: string | null;
  active: boolean;
}

export const criteriaApi = {
  async list(): Promise<Criterion[]> {
    const { data } = await apiClient.get<Criterion[]>("/criteria");
    return data;
  },
};
