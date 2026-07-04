import { Navigate, Outlet } from "react-router-dom";
import { LoadingState } from "@/components/ui/states";
import { paths } from "@/routes/paths";
import { useAuth } from "./use-auth";

/** Gate for authenticated routes. Wrap protected route trees with this. */
export function ProtectedRoute() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingState label="Memeriksa sesi..." />;
  }
  if (!isAuthenticated) {
    return <Navigate to={paths.login} replace />;
  }
  return <Outlet />;
}
