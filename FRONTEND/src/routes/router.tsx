import { createBrowserRouter } from "react-router-dom";
import { DashboardLayout } from "@/components/layout/dashboard-layout";
import { LoginPage } from "@/features/auth/login-page";
import { ProtectedRoute } from "@/features/auth/protected-route";
import { CategoriesPage } from "@/pages/categories/categories-page";
import { CriteriaPage } from "@/pages/criteria/criteria-page";
import { DashboardPage } from "@/pages/dashboard/dashboard-page";
import { PlaceholderPage } from "@/pages/placeholder-page";
import { ToysPage } from "@/pages/toys/toys-page";
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
          {
            path: paths.weightProfiles,
            element: (
              <PlaceholderPage
                title="Profil Bobot"
                description="Scenario weights — tiap profil = satu hasil pairwise tervalidasi."
              />
            ),
          },
          {
            path: paths.pairwise,
            element: (
              <PlaceholderPage
                title="Pairwise Comparison"
                description="AHP — perbandingan berpasangan antar kriteria (skala Saaty 1–9)."
              />
            ),
          },
          {
            path: paths.calculation,
            element: (
              <PlaceholderPage
                title="Kalkulasi & Publikasi"
                description="Sintesis SAW per profil → publikasi ke aplikasi mobile."
              />
            ),
          },
          {
            path: paths.results,
            element: (
              <PlaceholderPage
                title="Hasil Kalkulasi"
                description="Ranking global per profil bobot."
              />
            ),
          },
          {
            path: paths.reports,
            element: <PlaceholderPage title="Laporan" description="Arsip sesi kalkulasi AHP." />,
          },
          {
            path: paths.settings,
            element: (
              <PlaceholderPage title="Pengaturan" description="Kelola akun dan preferensi." />
            ),
          },
        ],
      },
    ],
  },
]);
