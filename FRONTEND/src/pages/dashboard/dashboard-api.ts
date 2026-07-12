import { apiClient } from "@/lib/api-client";

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

export interface PublishStatus {
  published: boolean;
  lastPublishedAt: string | null;
  stale: boolean;
}

export interface DashboardSummary {
  totalToys: number;
  totalCriteria: number;
  totalCategories: number;
  totalProfiles: number;
  categoryDistribution: { name: string; count: number }[];
  top5: { name: string; score: number }[];
  recentSessions: RunSummary[];
  publishStatus: PublishStatus;
}

export const dashboardApi = {
  async summary(): Promise<DashboardSummary> {
    const { data } = await apiClient.get<DashboardSummary>("/dashboard/summary");
    return data;
  },
};
