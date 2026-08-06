import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import {
  login as requestLogin,
  logout as requestLogout,
  reissue,
  withdraw as requestWithdraw,
} from "../api/authApi";
import { AUTH_EXPIRED_EVENT } from "../api/apiClient";
import { queryClient } from "../api/queryClient";
import type { AuthUser, LoginRequest } from "../types/auth";
import { AuthContext } from "./AuthContext";
import { getUserFromAccessToken, isAccessTokenExpired } from "./jwt";
import { tokenStorage } from "./tokenStorage";

function restoreUser(): AuthUser | null {
  const tokens = tokenStorage.get();
  if (!tokens) return null;

  const user = getUserFromAccessToken(tokens.accessToken);
  if (!user) tokenStorage.clear();
  return user;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(restoreUser);
  const [isRestoring, setIsRestoring] = useState(() => {
    const tokens = tokenStorage.get();
    return Boolean(tokens && isAccessTokenExpired(tokens.accessToken));
  });

  const clearAuth = useCallback(() => {
    tokenStorage.clear();
    setUser(null);
    setIsRestoring(false);
    queryClient.clear();
  }, []);

  useEffect(() => {
    window.addEventListener(AUTH_EXPIRED_EVENT, clearAuth);
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, clearAuth);
  }, [clearAuth]);

  useEffect(() => {
    const tokens = tokenStorage.get();
    if (!tokens || !isAccessTokenExpired(tokens.accessToken)) {
      setIsRestoring(false);
      return;
    }

    let active = true;
    reissue()
      .then((newTokens) => {
        const restoredUser = getUserFromAccessToken(newTokens.accessToken);
        if (!restoredUser)
          throw new Error("재발급된 인증 정보를 확인할 수 없습니다.");
        if (active) setUser(restoredUser);
      })
      .catch(() => {
        if (active) clearAuth();
      })
      .finally(() => {
        if (active) setIsRestoring(false);
      });

    return () => {
      active = false;
    };
  }, [clearAuth]);

  const login = useCallback(async (request: LoginRequest) => {
    const tokens = await requestLogin(request);
    const authenticatedUser = getUserFromAccessToken(tokens.accessToken);
    if (!authenticatedUser || isAccessTokenExpired(tokens.accessToken)) {
      throw new Error("로그인 응답의 인증 정보를 확인할 수 없습니다.");
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

  const withdraw = useCallback(
    async (password: string) => {
      const response = await requestWithdraw({ password });
      clearAuth();
      return response;
    },
    [clearAuth],
  );

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: user !== null,
      isRestoring,
      login,
      logout,
      withdraw,
    }),
    [isRestoring, login, logout, user, withdraw],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
