import { apiClient } from "@/lib/api-client";

export interface PrecheckItem {
  label: string;
  ok: boolean;
  detail: string;
}

export interface PrecheckResponse {
  allOk: boolean;
  items: PrecheckItem[];
}

export interface ProfileSummary {
  profileCode: string;
  profileName: string;
  cr: number;
  consistent: boolean;
  bestToyName: string | null;
}

export interface RunSummary {
  id: number;
  code: string;
  runAt: string;
  altCount: number;
  published: boolean;
  publishedAt: string | null;
  results: ProfileSummary[];
}

export interface RankingRow {
  rank: number;
  toyId: number;
  toyName: string;
  categoryName: string | null;
  sawScore: number;
}

export interface ProfileDetail {
  profileCode: string;
  profileName: string;
  cr: number;
  lambdaMax: number;
  ci: number;
  consistent: boolean;
  ranking: RankingRow[];
}

export interface RunDetail {
  id: number;
  code: string;
  runAt: string;
  altCount: number;
  published: boolean;
  publishedAt: string | null;
  results: ProfileDetail[];
}

export const calculationsApi = {
  async precheck(): Promise<PrecheckResponse> {
    const { data } = await apiClient.post<PrecheckResponse>("/calculations/precheck");
    return data;
  },
  async run(): Promise<RunSummary> {
    const { data } = await apiClient.post<RunSummary>("/calculations/run");
    return data;
  },
  async publish(id: number): Promise<RunSummary> {
    const { data } = await apiClient.post<RunSummary>(`/calculations/${id}/publish`);
    return data;
  },
  async list(): Promise<RunSummary[]> {
    const { data } = await apiClient.get<RunSummary[]>("/calculations");
    return data;
  },
  async detail(id: number): Promise<RunDetail> {
    const { data } = await apiClient.get<RunDetail>(`/calculations/${id}`);
    return data;
  },
};
