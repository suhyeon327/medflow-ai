import { Link } from "react-router";

export function NotFoundPage() {
  return (
    <main className="flex min-h-screen items-center justify-center px-4 text-center">
      <div>
        <h1 className="text-2xl font-bold">페이지를 찾을 수 없습니다.</h1>
        <Link
          to="/"
          className="mt-4 inline-block text-sm font-semibold text-blue-700"
        >
          홈으로 이동
        </Link>
      </div>
    </main>
  );
}
