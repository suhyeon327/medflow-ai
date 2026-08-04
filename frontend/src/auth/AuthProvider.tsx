import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { login as requestLogin, logout as requestLogout } from '../api/authApi';
import { AUTH_EXPIRED_EVENT } from '../api/apiClient';
import { queryClient } from '../api/queryClient';
import type { AuthUser, LoginRequest } from '../types/auth';
import { AuthContext } from './AuthContext';
import { getUserFromAccessToken, isAccessTokenExpired } from './jwt';
import { tokenStorage } from './tokenStorage';

function restoreUser(): AuthUser | null {
  const tokens = tokenStorage.get();
  if (!tokens || isAccessTokenExpired(tokens.accessToken)) {
    tokenStorage.clear();
    return null;
  }
  const user = getUserFromAccessToken(tokens.accessToken);
  if (!user) tokenStorage.clear();
  return user;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(restoreUser);

  const clearAuth = useCallback(() => {
    tokenStorage.clear();
    setUser(null);
    queryClient.clear();
  }, []);

  useEffect(() => {
    window.addEventListener(AUTH_EXPIRED_EVENT, clearAuth);
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, clearAuth);
  }, [clearAuth]);

  const login = useCallback(async (request: LoginRequest) => {
    const tokens = await requestLogin(request);
    const authenticatedUser = getUserFromAccessToken(tokens.accessToken);
    if (!authenticatedUser || isAccessTokenExpired(tokens.accessToken)) {
      throw new Error('로그인 응답의 인증 정보를 확인할 수 없습니다.');
    }
    tokenStorage.set(tokens);
    setUser(authenticatedUser);
    return authenticatedUser;
  }, []);

  const logout = useCallback(async () => {
    const refreshToken = tokenStorage.get()?.refreshToken;
    try {
      if (refreshToken) await requestLogout(refreshToken);
    } finally {
      clearAuth();
    }
  }, [clearAuth]);

  const value = useMemo(() => ({ user, isAuthenticated: user !== null, login, logout }), [login, logout, user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}