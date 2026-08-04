export interface ApiErrorPayload {
  code: string;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error?: ApiErrorPayload | null;
  timestamp: string;
}