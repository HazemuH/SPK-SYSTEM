import { apiClient } from "@/lib/api-client";

export interface WeightProfile {
  id: number;
  code: string;
  name: string;
  shortName: string | null;
  icon: string | null;
  description: string | null;
  isDefault: boolean;
  active: boolean;
  cr: number;
  lambdaMax: number;
  ci: number;
  consistent: boolean;
  weights: Record<string, number>;
}

export interface WeightProfileInput {
  name: string;
  shortName?: string;
  icon?: string;
  description?: string;
}

/** One upper-triangle Saaty comparison: how much more important rowCode is than colCode. */
export interface PairwiseEntry {
  rowCode: string;
  colCode: string;
  value: number;
}

export const weightProfilesApi = {
  async list(): Promise<WeightProfile[]> {
    const { data } = await apiClient.get<WeightProfile[]>("/weight-profiles");
    return data;
  },
  async get(id: number): Promise<WeightProfile> {
    const { data } = await apiClient.get<WeightProfile>(`/weight-profiles/${id}`);
    return data;
  },
  async create(input: WeightProfileInput): Promise<WeightProfile> {
    const { data } = await apiClient.post<WeightProfile>("/weight-profiles", input);
    return data;
  },
  async update(id: number, input: WeightProfileInput): Promise<WeightProfile> {
    const { data } = await apiClient.put<WeightProfile>(`/weight-profiles/${id}`, input);
    return data;
  },
  async remove(id: number): Promise<void> {
    await apiClient.delete(`/weight-profiles/${id}`);
  },
  async computePairwise(id: number, entries: PairwiseEntry[]): Promise<WeightProfile> {
    const { data } = await apiClient.put<WeightProfile>(`/weight-profiles/${id}/pairwise`, {
      entries,
    });
    return data;
  },
};
