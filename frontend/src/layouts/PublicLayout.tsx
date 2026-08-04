import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { HOSPITALS_PATH, LOGIN_PATH, ROLE_HOME_PATH, SIGNUP_PATH } from '../routes/routePaths';

export function PublicLayout() {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4 sm:px-6">
          <Link to={HOSPITALS_PATH} className="text-lg font-bold text-blue-700">MedFlow AI</Link>
          <nav className="flex items-center gap-2" aria-label="주요 메뉴">
            <Link to={HOSPITALS_PATH} className="rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100">병원 찾기</Link>
            {user ? (
              <Link to={ROLE_HOME_PATH[user.role]} className="rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white hover:bg-blue-700">내 화면</Link>
            ) : (
              <>
                <Link to={LOGIN_PATH} className="rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100">로그인</Link>
                <Link to={SIGNUP_PATH} className="rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white hover:bg-blue-700">회원가입</Link>
              </>
            )}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-10 sm:px-6"><Outlet /></main>
    </div>
  );
}