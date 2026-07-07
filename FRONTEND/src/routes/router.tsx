import { createBrowserRouter } from "react-router-dom";
import { DashboardLayout } from "@/components/layout/dashboard-layout";
import { LoginPage } from "@/features/auth/login-page";
import { ProtectedRoute } from "@/features/auth/protected-route";
import { CalculationPage } from "@/pages/calculation/calculation-page";
import { CategoriesPage } from "@/pages/categories/categories-page";
import { CriteriaPage } from "@/pages/criteria/criteria-page";
import { DashboardPage } from "@/pages/dashboard/dashboard-page";
import { PairwisePage } from "@/pages/pairwise/pairwise-page";
import { ReportsPage } from "@/pages/reports/reports-page";
import { ResultsPage } from "@/pages/results/results-page";
import { SettingsPage } from "@/pages/settings/settings-page";
import { ToysPage } from "@/pages/toys/toys-page";
import { WeightProfilesPage } from "@/pages/weight-profiles/weight-profiles-page";
import { paths } from "./paths";

export const router = createBrowserRouter([
  { path: paths.login, element: <LoginPage /> },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          { path: paths.dashboard, element: <DashboardPage /> },
          { path: paths.toys, element: <ToysPage /> },
          { path: paths.categories, element: <CategoriesPage /> },
          { path: paths.criteria, element: <CriteriaPage /> },
          { path: paths.weightProfiles, element: <WeightProfilesPage /> },
          { path: paths.pairwise, element: <PairwisePage /> },
          { path: paths.calculation, element: <CalculationPage /> },
          { path: paths.results, element: <ResultsPage /> },
          { path: paths.reports, element: <ReportsPage /> },
          { path: paths.settings, element: <SettingsPage /> },
        ],
      },
    ],
  },
]);
