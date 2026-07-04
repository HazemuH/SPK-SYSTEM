import { createBrowserRouter } from "react-router-dom";
import { DashboardLayout } from "@/components/layout/dashboard-layout";
import { LoginPage } from "@/features/auth/login-page";
import { ProtectedRoute } from "@/features/auth/protected-route";
import { DashboardPage } from "@/pages/dashboard-page";
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
          // Add new management pages here.
        ],
      },
    ],
  },
]);
