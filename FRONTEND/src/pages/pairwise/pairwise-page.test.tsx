import { fireEvent, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { renderWithProviders } from "@/test/test-utils";

const profiles = [
  {
    id: 1,
    code: "balanced",
    name: "Seimbang",
    shortName: "Seimbang",
    icon: "scale",
    description: "",
    cr: 0.04,
    lambdaMax: 3.01,
    ci: 0.02,
    consistent: true,
    weights: { keamanan: 0.5, edukasi: 0.3, harga: 0.2 },
  },
];
const criteria = [
  {
    id: 1,
    code: "keamanan",
    no: 1,
    name: "Keamanan",
    type: "benefit",
    description: "",
    abbr: "Aman",
    active: true,
  },
  {
    id: 2,
    code: "edukasi",
    no: 2,
    name: "Edukasi",
    type: "benefit",
    description: "",
    abbr: "Edu",
    active: true,
  },
  {
    id: 3,
    code: "harga",
    no: 3,
    name: "Harga",
    type: "cost",
    description: "",
    abbr: "Harga",
    active: true,
  },
];

vi.mock("@/pages/weight-profiles/weight-profiles-api", () => ({
  weightProfilesApi: {
    list: vi.fn().mockResolvedValue(profiles),
    computePairwise: vi.fn().mockResolvedValue(profiles[0]),
  },
}));
vi.mock("@/pages/criteria/criteria-api", () => ({
  criteriaApi: { list: vi.fn().mockResolvedValue(criteria) },
}));

const { PairwisePage } = await import("./pairwise-page");

describe("PairwisePage", () => {
  it("shows the grouped comparison list and a live consistency badge", async () => {
    renderWithProviders(<PairwisePage />);
    // Grouped by the row criterion (one heading per row criterion).
    expect((await screen.findAllByText(/Seberapa penting/)).length).toBeGreaterThan(0);
    // A live CR estimate is shown.
    expect(screen.getAllByText(/Perkiraan:.*CR/).length).toBeGreaterThan(0);
  });

  it("updates the sentence when the slider moves", async () => {
    renderWithProviders(<PairwisePage />);
    const slider = (await screen.findByLabelText(
      /Bandingkan Keamanan dengan Edukasi/,
    )) as HTMLInputElement;
    // Drag to the extreme right (index 8 = value 9 = row/Keamanan wins outright).
    fireEvent.change(slider, { target: { value: "8" } });
    expect(
      await screen.findByText(/Keamanan mutlak lebih penting dari Edukasi/),
    ).toBeInTheDocument();
  });

  it("shows a 'saved' banner after computing", async () => {
    renderWithProviders(<PairwisePage />);
    const compute = (await screen.findAllByRole("button", { name: /Hitung Bobot & CR/ }))[0];
    fireEvent.click(compute);
    expect(await screen.findByText(/berhasil disimpan/)).toBeInTheDocument();
  });
});
