export type UserRole = 'PATIENT' | 'DOCTOR' | 'ADMIN';
export type SignupRole = Exclude<UserRole, 'ADMIN'>;

export interface AuthUser {
  email: string;
  role: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  role: SignupRole;
}

export interface SignupResponse {
  id: number;
  email: string;
  role: SignupRole;
}

export interface TokenResponse {
  grantType: string;
  accessToken: string;
  refreshToken: string;
}