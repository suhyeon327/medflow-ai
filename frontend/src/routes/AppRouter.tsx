import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { AppLayout } from '../layouts/AppLayout';
import { PublicLayout } from '../layouts/PublicLayout';
import { DoctorDetailPage } from '../pages/DoctorDetailPage';
import { HospitalDetailPage } from '../pages/HospitalDetailPage';
import { HospitalListPage } from '../pages/HospitalListPage';
import { LoginPage } from '../pages/LoginPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { RoleHomePage } from '../pages/RoleHomePage';
import { SignupPage } from '../pages/SignupPage';
import { UnauthorizedPage } from '../pages/UnauthorizedPage';
import { WithdrawPage } from '../pages/WithdrawPage';
import { ProtectedRoute, PublicOnlyRoute, RoleRoute } from './RouteGuards';
import { HOSPITALS_PATH, LOGIN_PATH, ROLE_HOME_PATH, SIGNUP_PATH, UNAUTHORIZED_PATH, WITHDRAW_PATH } from './routePaths';

function HomeRedirect() {
  const { user } = useAuth();
  return <Navigate to={user ? ROLE_HOME_PATH[user.role] : HOSPITALS_PATH} replace />;
}

export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route element={<PublicOnlyRoute />}>
        <Route path={LOGIN_PATH} element={<LoginPage />} />
        <Route path={SIGNUP_PATH} element={<SignupPage />} />
      </Route>

      <Route element={<PublicLayout />}>
        <Route path={HOSPITALS_PATH} element={<HospitalListPage />} />
        <Route path="/hospitals/:hospitalId" element={<HospitalDetailPage />} />
        <Route path="/doctors/:doctorId" element={<DoctorDetailPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route path={UNAUTHORIZED_PATH} element={<UnauthorizedPage />} />
        <Route element={<AppLayout />}>
          <Route element={<RoleRoute allowedRole="PATIENT" />}>
            <Route path={ROLE_HOME_PATH.PATIENT} element={<RoleHomePage />} />
            <Route path={WITHDRAW_PATH} element={<WithdrawPage />} />
          </Route>
          <Route element={<RoleRoute allowedRole="DOCTOR" />}>
            <Route path={ROLE_HOME_PATH.DOCTOR} element={<RoleHomePage />} />
          </Route>
          <Route element={<RoleRoute allowedRole="ADMIN" />}>
            <Route path={ROLE_HOME_PATH.ADMIN} element={<RoleHomePage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}