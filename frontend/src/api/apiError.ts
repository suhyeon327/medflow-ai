import type { ApiErrorPayload } from '../types/api';

export class ApiError extends Error {
  constructor(message: string, public readonly status?: number, public readonly code?: string) {
    super(message);
    this.name = 'ApiError';
  }
}

export function getApiErrorMessage(error: unknown): string {
  return error instanceof ApiError
    ? error.message
    : '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.';
}

export function toApiError(status: number, error?: ApiErrorPayload | null): ApiError {
  return new ApiError(error?.message ?? '요청을 처리하지 못했습니다.', status, error?.code);
}