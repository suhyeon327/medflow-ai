import type { UserRole } from '../types/auth';

export const LOGIN_PATH = '/login';
export const UNAUTHORIZED_PATH = '/unauthorized';

export const ROLE_HOME_PATH: Record<UserRole, string> = {
  PATIENT: '/patient',
  DOCTOR: '/doctor',
  ADMIN: '/admin',
};