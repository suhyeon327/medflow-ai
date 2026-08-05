import type { UserRole } from '../types/auth';

export const LOGIN_PATH = '/login';
export const SIGNUP_PATH = '/signup';
export const UNAUTHORIZED_PATH = '/unauthorized';
export const WITHDRAW_PATH = '/account/withdraw';
export const PATIENT_RESERVATIONS_PATH = '/patient/reservations';
export const PATIENT_PROFILE_PATH = '/patient/profile';
export const DOCTOR_PROFILE_PATH = '/doctor/profile';
export const DOCTOR_SCHEDULES_PATH = '/doctor/schedules';
export const ADMIN_DOCTOR_DETAIL_PATH = (doctorId: number) => `/admin/doctors/${doctorId}`;
export const ADMIN_USERS_PATH = '/admin/users';
export const ADMIN_HOSPITALS_PATH = '/admin/hospitals';
export const HOSPITALS_PATH = '/hospitals';
export const HOSPITAL_DETAIL_PATH = (hospitalId: number) => `/hospitals/${hospitalId}`;
export const DOCTOR_DETAIL_PATH = (doctorId: number) => `/doctors/${doctorId}`;

export const ROLE_HOME_PATH: Record<UserRole, string> = {
  PATIENT: '/patient',
  DOCTOR: '/doctor',
  ADMIN: '/admin',
};
