import { screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { renderWithProviders } from "@/test/test-utils";
import type { DashboardSummary } from "./dashboard-api";

const fixture: DashboardSummary = {
  totalToys: 50,
  totalCriteria: 10,
  totalCategories: 8,
  totalProfiles: 5,
  categoryDistribution: [{ name: "Edukatif", count: 6 }],
  top5: [{ name: "Lego Technic Excavator", score: 0.794 }],
  recentSessions: [],
  publishStatus: { published: true, lastPublishedAt: "2026-07-12T00:00:00Z", stale: false },
};

const summaryMock = vi.fn().mockResolvedValue(fixture);
vi.mock("./dashboard-api", () => ({
  dashboardApi: { summary: () => summaryMock() },
}));

// Import after the mock is registered.
const { DashboardPage } = await import("./dashboard-page");

describe("DashboardPage", () => {
  it("renders the summary stats and top-5", async () => {
    summaryMock.mockResolvedValueOnce(fixture);
    renderWithProviders(<DashboardPage />);
    expect(await screen.findByText("Total Mainan")).toBeInTheDocument();
    expect(await screen.findByText("Lego Technic Excavator")).toBeInTheDocument();
    // 50 total toys stat
    expect(await screen.findByText("50")).toBeInTheDocument();
  });

  it("shows the stale banner when the published snapshot is out of date", async () => {
    summaryMock.mockResolvedValueOnce({
      ...fixture,
      publishStatus: { published: true, lastPublishedAt: "2026-07-12T00:00:00Z", stale: true },
    });
    renderWithProviders(<DashboardPage />);
    expect(await screen.findByText(/Ada perubahan sejak publikasi terakhir/)).toBeInTheDocument();
    expect(await screen.findByText("Ke Kalkulasi")).toBeInTheDocument();
  });
});
