import type {
  LoginRequest,
  SignupRequest,
  SignupResponse,
  TokenResponse,
  WithdrawRequest,
  WithdrawResponse,
} from "../types/auth";
import { apiClient, reissueTokens } from "./apiClient";

export function signup(request: SignupRequest): Promise<SignupResponse> {
  return apiClient<SignupResponse>(
    { url: "/api/v1/auth/signup", method: "POST", data: request },
    false,
  );
}

export function login(request: LoginRequest): Promise<TokenResponse> {
  return apiClient<TokenResponse>(
    { url: "/api/v1/auth/login", method: "POST", data: request },
    false,
  );
}

export function reissue(): Promise<TokenResponse> {
  return reissueTokens();
}

export function logout(refreshToken: string): Promise<void> {
  return apiClient<void>({
    url: "/api/v1/auth/logout",
    method: "POST",
    data: { refreshToken },
  });
}

export function withdraw(request: WithdrawRequest): Promise<WithdrawResponse> {
  return apiClient<WithdrawResponse>({
    url: "/api/v1/auth/withdraw",
    method: "DELETE",
    data: request,
  });
}
