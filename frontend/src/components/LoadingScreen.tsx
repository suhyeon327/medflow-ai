export function LoadingScreen({ message = '처리 중입니다.' }: { message?: string }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50" role="status">
      <p className="text-sm font-medium text-slate-600">{message}</p>
    </div>
  );
}