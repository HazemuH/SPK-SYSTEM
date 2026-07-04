import type { Toy } from "./toys-types";
// import { apiClient } from "@/lib/api-client";
// import type { PageResponse } from "@/lib/types";

/**
 * Data access for toys. Currently returns MOCK data so the UI runs without a
 * backend toys endpoint yet.
 *
 * ➜ To use the real API, delete the mock and uncomment the real call once the
 *   backend exposes `/toys` (scaffold it with BACKEND/scripts/new-feature.sh):
 *
 *   async list(): Promise<Toy[]> {
 *     const { data } = await apiClient.get<PageResponse<Toy>>("/toys");
 *     return data.content;
 *   }
 */
const MOCK_TOYS: Toy[] = [
  { id: "1", name: "Lego Classic", category: "Konstruksi", price: 250000, active: true },
  { id: "2", name: "Rubik's Cube", category: "Puzzle", price: 45000, active: true },
  { id: "3", name: "Boneka Beruang", category: "Boneka", price: 120000, active: true },
  { id: "4", name: "Mobil Remote", category: "Elektronik", price: 320000, active: false },
  { id: "5", name: "Puzzle Kayu", category: "Edukasi", price: 60000, active: true },
];

export const toysApi = {
  async list(): Promise<Toy[]> {
    // Simulate network latency so loading states are visible during development.
    await new Promise((resolve) => setTimeout(resolve, 400));
    return MOCK_TOYS;
  },
};
