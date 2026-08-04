import type { TokenResponse } from '../types/auth';

const TOKEN_STORAGE_KEY = 'medflow.auth.tokens';

export const tokenStorage = {
  get(): TokenResponse | null {
    const storedTokens = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (!storedTokens) return null;

    try {
      return JSON.parse(storedTokens) as TokenResponse;
    } catch {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      return null;
    }
  },
  set(tokens: TokenResponse): void {
    localStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(tokens));
  },
  clear(): void {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  },
};