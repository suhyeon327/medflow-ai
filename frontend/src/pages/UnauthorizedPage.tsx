import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { LOGIN_PATH, ROLE_HOME_PATH } from '../routes/routePaths';

export function UnauthorizedPage() {
  const { user } = useAuth();
  const destination = user ? ROLE_HOME_PATH[user.role] : LOGIN_PATH;

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <section className="w-full max-w-md rounded-xl bg-white p-8 text-center shadow-sm">
        <p className="text-sm font-semibold text-red-600">403</p>
        <h1 className="mt-2 text-2xl font-bold">접근 권한이 없습니다.</h1>
        <p className="mt-3 text-slate-600">현재 계정의 역할로는 이 페이지를 볼 수 없습니다.</p>
        <Link to={destination} replace className="mt-6 inline-block rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700">
          돌아가기
        </Link>
      </section>
    </main>
  );
}