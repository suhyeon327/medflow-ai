export type UserRole = "PATIENT" | "DOCTOR" | "ADMIN";
export type SignupRole = Exclude<UserRole, "ADMIN">;
export type Gender = "MALE" | "FEMALE";
export type DoctorProfileStatus = "PENDING" | "ACTIVE" | "REJECTED";

export interface AuthUser {
  email: string;
  role: UserRole;
}
export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupAccountForm {
  email: string;
  password: string;
  passwordConfirm: string;
}

export interface PatientSignupForm {
  name: string;
  birth: string;
  gender: Gender;
  phone: string;
}

export interface DoctorSignupForm {
  hospitalId: number | null;
  name: string;
  licenseNumber: string;
  specialty: string;
  introduction: string;
  contact: string;
}

export type SignupRequest =
  | {
      email: string;
      password: string;
      role: "PATIENT";
      patient: PatientSignupForm;
    }
  | {
      email: string;
      password: string;
      role: "DOCTOR";
      doctor: Omit<DoctorSignupForm, "hospitalId"> & { hospitalId: number };
    };

export interface SignupResponse {
  id: number;
  email: string;
  role: SignupRole;
  profileId: number;
  profileStatus: DoctorProfileStatus | null;
}

export interface TokenResponse {
  grantType: string;
  accessToken: string;
  refreshToken: string;
}
export interface WithdrawRequest {
  password: string;
}
export interface WithdrawResponse {
  id: number;
  deleteAt: string;
  message: string;
}
