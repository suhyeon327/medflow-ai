export type UserRole = 'PATIENT' | 'DOCTOR' | 'ADMIN';

export interface AuthUser {
  email: string;
  role: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  grantType: string;
  accessToken: string;
  refreshToken: string;
}