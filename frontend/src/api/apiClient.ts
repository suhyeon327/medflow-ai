import axios, { type AxiosError, type AxiosRequestConfig, type AxiosResponse } from 'axios';
import { tokenStorage } from '../auth/tokenStorage';
import type { ApiResponse } from '../types/api';
import type { TokenResponse } from '../types/auth';
import { ApiError, toApiError } from './apiError';

export const AUTH_EXPIRED_EVENT = 'medflow:auth-expired';
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '');

if (!API_BASE_URL) throw new Error('VITE_API_BASE_URL 환경변수가 설정되지 않았습니다.');

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
  timeout: 10_000,
});

let reissuePromise: Promise<TokenResponse> | null = null;

function unwrapResponse<T>(response: AxiosResponse<ApiResponse<T>>): T {
  if (!response.data.success) throw toApiError(response.status, response.data.error);
  return response.data.data as T;
}

function normalizeError(error: unknown): ApiError {
  if (error instanceof ApiError) return error;

  if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
    if (!error.response) {
      return new ApiError('서버에 연결할 수 없습니다. 네트워크 상태를 확인해 주세요.');
    }
    return toApiError(error.response.status, error.response.data?.error);
  }

  return new ApiError('요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.');
}

export function reissueTokens(): Promise<TokenResponse> {
  const refreshToken = tokenStorage.get()?.refreshToken;
  if (!refreshToken) return Promise.reject(new ApiError('로그인이 필요합니다.', 401));

  if (!reissuePromise) {
    reissuePromise = axiosClient
      .post<ApiResponse<TokenResponse>>('/api/v1/auth/reissue', { refreshToken })
      .then(unwrapResponse)
      .then((tokens) => {
        tokenStorage.set(tokens);
        return tokens;
      })
      .catch((error: unknown) => { throw normalizeError(error); })
      .finally(() => { reissuePromise = null; });
  }

  return reissuePromise;
}

export async function apiClient<T>(
  config: AxiosRequestConfig,
  authenticated = true,
): Promise<T> {
  const accessToken = authenticated ? tokenStorage.get()?.accessToken : null;

  try {
    const response = await axiosClient.request<ApiResponse<T>>({
      ...config,
      headers: {
        ...config.headers,
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      },
    });
    return unwrapResponse(response);
  } catch (error) {
    const axiosError = error as AxiosError<ApiResponse<unknown>>;
    const shouldReissue = authenticated && axios.isAxiosError(error) && axiosError.response?.status === 401;

    if (!shouldReissue) throw normalizeError(error);

    try {
      const tokens = await reissueTokens();
      const retriedResponse = await axiosClient.request<ApiResponse<T>>({
        ...config,
        headers: { ...config.headers, Authorization: `Bearer ${tokens.accessToken}` },
      });
      return unwrapResponse(retriedResponse);
    } catch (reissueError) {
      window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
      throw normalizeError(reissueError);
    }
  }
}