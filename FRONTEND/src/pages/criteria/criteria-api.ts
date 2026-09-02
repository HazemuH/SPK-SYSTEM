import { apiClient } from "@/lib/api-client";

/** One of a criterion's five subcriteria (S1 best … S5 worst) with its AHP priority. */
export interface CriterionLevel {
  level: number;
  code: string;
  label: string;
  priority: number;
}

export interface Criterion {
  id: number;
  code: string;
  no: number;
  name: string;
  type: "benefit" | "cost";
  description: string | null;
  abbr: string | null;
  active: boolean;
  levels: CriterionLevel[];
}

export interface CriterionCreate {
  name: string;
  type: "benefit" | "cost";
  description?: string;
  abbr?: string;
}

export interface CriterionUpdate {
  name: string;
  description?: string;
  abbr?: string;
  active?: boolean;
}

export const criteriaApi = {
  async list(): Promise<Criterion[]> {
    const { data } = await apiClient.get<Criterion[]>("/criteria");
    return data;
  },
  async create(input: CriterionCreate): Promise<Criterion> {
    const { data } = await apiClient.post<Criterion>("/criteria", input);
    return data;
  },
  async update(id: number, input: CriterionUpdate): Promise<Criterion> {
    const { data } = await apiClient.put<Criterion>(`/criteria/${id}`, input);
    return data;
  },
  async remove(id: number): Promise<void> {
    await apiClient.delete(`/criteria/${id}`);
  },
};
