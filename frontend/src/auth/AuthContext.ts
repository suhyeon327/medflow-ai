import { createContext, useContext } from 'react';
import type { AuthUser, LoginRequest } from '../types/auth';

export interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<AuthUser>;
  logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth는 AuthProvider 내부에서 사용해야 합니다.');
  return context;
}