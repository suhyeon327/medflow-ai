import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import type { UserRole } from '../types/auth';
import { LOGIN_PATH, ROLE_HOME_PATH, UNAUTHORIZED_PATH } from './routePaths';

export function PublicOnlyRoute() {
  const { user } = useAuth();
  return user ? <Navigate to={ROLE_HOME_PATH[user.role]} replace /> : <Outlet />;
}

export function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  return isAuthenticated
    ? <Outlet />
    : <Navigate to={LOGIN_PATH} replace state={{ from: location.pathname }} />;
}

export function RoleRoute({ allowedRole }: { allowedRole: UserRole }) {
  const { user } = useAuth();
  return user?.role === allowedRole
    ? <Outlet />
    : <Navigate to={UNAUTHORIZED_PATH} replace />;
}