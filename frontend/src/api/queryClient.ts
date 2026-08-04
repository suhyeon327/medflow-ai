import { QueryClient } from '@tanstack/react-query';
import { ApiError } from './apiError';

const shouldRetry = (failureCount: number, error: unknown): boolean => {
  if (error instanceof ApiError && (error.status === 401 || error.status === 403)) return false;
  return failureCount < 2;
};

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 60_000, retry: shouldRetry, refetchOnWindowFocus: false },
    mutations: { retry: false },
  },
});