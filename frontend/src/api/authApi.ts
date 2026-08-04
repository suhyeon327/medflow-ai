import type { LoginRequest, TokenResponse } from '../types/auth';
import { apiClient } from './apiClient';

export function login(request: LoginRequest): Promise<TokenResponse> {
  return apiClient<TokenResponse>('/api/v1/auth/login', {
    method: 'POST', body: request, authenticated: false,
  });
}

export function logout(refreshToken: string): Promise<void> {
  return apiClient<void>('/api/v1/auth/logout', {
    method: 'POST', body: { refreshToken },
  });
}