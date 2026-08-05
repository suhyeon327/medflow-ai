import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { AppLayout } from '../layouts/AppLayout';
import { AdminUsersPage } from '../pages/AdminUsersPage';
import { AdminHospitalsPage } from '../pages/AdminHospitalsPage';
import { PublicLayout } from '../layouts/PublicLayout';
import { DoctorDetailPage } from '../pages/DoctorDetailPage';
import { DoctorProfilePage } from '../pages/DoctorProfilePage';
import { DoctorSchedulesPage } from '../pages/DoctorSchedulesPage';
import { DoctorReservationsPage } from '../pages/DoctorReservationsPage';
import { AdminDoctorDetailPage } from '../pages/AdminDoctorDetailPage';
import { HospitalDetailPage } from '../pages/HospitalDetailPage';
import { HospitalListPage } from '../pages/HospitalListPage';
import { LoginPage } from '../pages/LoginPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { PatientReservationsPage } from '../pages/PatientReservationsPage';
import { PatientProfilePage } from '../pages/PatientProfilePage';
import { AdminReservationsPage } from '../pages/AdminReservationsPage';
import { RoleHomePage } from '../pages/RoleHomePage';
import { SignupPage } from '../pages/SignupPage';
import { UnauthorizedPage } from '../pages/UnauthorizedPage';
import { WithdrawPage } from '../pages/WithdrawPage';
import { ProtectedRoute, PublicOnlyRoute, RoleRoute } from './RouteGuards';
import { ADMIN_HOSPITALS_PATH, ADMIN_RESERVATIONS_PATH, ADMIN_USERS_PATH, DOCTOR_PROFILE_PATH, DOCTOR_RESERVATIONS_PATH, DOCTOR_SCHEDULES_PATH, HOSPITALS_PATH, LOGIN_PATH, PATIENT_PROFILE_PATH, PATIENT_RESERVATIONS_PATH, ROLE_HOME_PATH, SIGNUP_PATH, UNAUTHORIZED_PATH, WITHDRAW_PATH } from './routePaths';

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
            <Route path={PATIENT_RESERVATIONS_PATH} element={<PatientReservationsPage />} />
            <Route path={PATIENT_PROFILE_PATH} element={<PatientProfilePage />} />
            <Route path={WITHDRAW_PATH} element={<WithdrawPage />} />
          </Route>
          <Route element={<RoleRoute allowedRole="DOCTOR" />}>
            <Route path={ROLE_HOME_PATH.DOCTOR} element={<RoleHomePage />} />
            <Route path={DOCTOR_PROFILE_PATH} element={<DoctorProfilePage />} />
            <Route path={DOCTOR_SCHEDULES_PATH} element={<DoctorSchedulesPage />} />
            <Route path={DOCTOR_RESERVATIONS_PATH} element={<DoctorReservationsPage />} />
          </Route>
          <Route element={<RoleRoute allowedRole="ADMIN" />}>
            <Route path={ROLE_HOME_PATH.ADMIN} element={<RoleHomePage />} />
            <Route path={ADMIN_HOSPITALS_PATH} element={<AdminHospitalsPage />} />
            <Route path={ADMIN_RESERVATIONS_PATH} element={<AdminReservationsPage />} />
            <Route path={ADMIN_USERS_PATH} element={<AdminUsersPage />} />
            <Route path="/admin/doctors/:doctorId" element={<AdminDoctorDetailPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
