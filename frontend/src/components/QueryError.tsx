import { getApiErrorMessage } from '../api/apiError';

interface QueryErrorProps {
  error: unknown;
  onRetry?: () => void;
}

export function QueryError({ error, onRetry }: QueryErrorProps) {
  return (
    <div role="alert" className="rounded-lg border border-red-200 bg-red-50 p-5 text-red-800">
      <p className="font-semibold">정보를 불러오지 못했습니다.</p>
      <p className="mt-1 text-sm">{getApiErrorMessage(error)}</p>
      {onRetry && (
        <button type="button" onClick={onRetry} className="mt-4 rounded-md border border-red-300 bg-white px-3 py-2 text-sm font-semibold hover:bg-red-100">
          다시 시도
        </button>
      )}
    </div>
  );
}