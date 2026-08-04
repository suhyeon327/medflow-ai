import { tokenStorage } from '../auth/tokenStorage';
import type { ApiResponse } from '../types/api';
import { ApiError, toApiError } from './apiError';

export const AUTH_EXPIRED_EVENT = 'medflow:auth-expired';
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '');

interface ApiRequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown;
  authenticated?: boolean;
}

export async function apiClient<T>(
  path: string,
  { body, authenticated = true, headers, ...options }: ApiRequestOptions = {},
): Promise<T> {
  if (!API_BASE_URL) throw new Error('VITE_API_BASE_URL 환경변수가 설정되지 않았습니다.');

  const requestHeaders = new Headers(headers);
  requestHeaders.set('Accept', 'application/json');
  if (body !== undefined) requestHeaders.set('Content-Type', 'application/json');

  const accessToken = authenticated ? tokenStorage.get()?.accessToken : null;
  if (accessToken) requestHeaders.set('Authorization', `Bearer ${accessToken}`);

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: requestHeaders,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError('서버에 연결할 수 없습니다. 네트워크 상태를 확인해 주세요.');
  }

  let payload: ApiResponse<T> | null = null;
  try {
    payload = (await response.json()) as ApiResponse<T>;
  } catch {
    // JSON이 아닌 오류 응답은 HTTP 상태를 기준으로 처리한다.
  }

  if (response.status === 401 && authenticated) {
    window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
  }
  if (!response.ok || !payload?.success) {
    throw toApiError(response.status, payload?.error);
  }
  return payload.data as T;
}