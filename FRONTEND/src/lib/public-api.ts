import { apiClient } from "@/lib/api-client";

/**
 * The published toy detail (`/public/toys/{id}`). We only need the normalized
 * r_ij matrix here (it's profile-independent — normalization is global over the
 * active toys), used to draw the radar in the admin Hasil view.
 */
export interface PublicToyDetail {
  globalRank: number;
  normalized: Record<string, number>;
}

export const publicApi = {
  async toyDetail(id: number | string): Promise<PublicToyDetail> {
    const { data } = await apiClient.get<PublicToyDetail>(`/public/toys/${id}`);
    return data;
  },
};
