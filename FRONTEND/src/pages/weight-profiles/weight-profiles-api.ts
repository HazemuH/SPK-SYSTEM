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

export const weightProfilesApi = {
  async list(): Promise<WeightProfile[]> {
    const { data } = await apiClient.get<WeightProfile[]>("/weight-profiles");
    return data;
  },
};
