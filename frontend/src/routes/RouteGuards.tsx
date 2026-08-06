import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { LoadingScreen } from "../components/LoadingScreen";
import type { UserRole } from "../types/auth";
import { LOGIN_PATH, ROLE_HOME_PATH, UNAUTHORIZED_PATH } from "./routePaths";

export function PublicOnlyRoute() {
  const { user, isRestoring } = useAuth();
  if (isRestoring)
    return <LoadingScreen message="로그인 상태를 확인하고 있습니다." />;
  return user ? (
    <Navigate to={ROLE_HOME_PATH[user.role]} replace />
  ) : (
    <Outlet />
  );
}

export function ProtectedRoute() {
  const { isAuthenticated, isRestoring } = useAuth();
  const location = useLocation();
  if (isRestoring)
    return <LoadingScreen message="로그인 상태를 확인하고 있습니다." />;
  return isAuthenticated ? (
    <Outlet />
  ) : (
    <Navigate to={LOGIN_PATH} replace state={{ from: location.pathname }} />
  );
}

export function RoleRoute({ allowedRole }: { allowedRole: UserRole }) {
  const { user } = useAuth();
  return user?.role === allowedRole ? (
    <Outlet />
  ) : (
    <Navigate to={UNAUTHORIZED_PATH} replace />
  );
}
