import axios, { AxiosError, type AxiosRequestConfig } from 'axios';
import { tokenStorage } from '../auth/tokenStorage';
import type { ApiResponse } from '../types/api';
import { ApiError, toApiError } from './apiError';

export const AUTH_EXPIRED_EVENT = 'medflow:auth-expired';
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '');

if (!API_BASE_URL) throw new Error('VITE_API_BASE_URL 환경변수가 설정되지 않았습니다.');

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
  timeout: 10_000,
});

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

    if (!response.data.success) {
      throw toApiError(response.status, response.data.error);
    }

    return response.data.data as T;
  } catch (error) {
    if (error instanceof ApiError) throw error;

    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<ApiResponse<unknown>>;
      const status = axiosError.response?.status;

      if (!axiosError.response) {
        throw new ApiError('서버에 연결할 수 없습니다. 네트워크 상태를 확인해 주세요.');
      }

      if (status === 401 && authenticated) {
        window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
      }

      throw toApiError(status ?? 500, axiosError.response.data?.error);
    }

    throw new ApiError('요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.');
  }
}