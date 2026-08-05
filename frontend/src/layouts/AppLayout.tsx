import { useState } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ADMIN_USERS_PATH, HOSPITALS_PATH, LOGIN_PATH, PATIENT_RESERVATIONS_PATH, WITHDRAW_PATH } from '../routes/routePaths';

const ROLE_LABEL = { PATIENT: '환자', DOCTOR: '의사', ADMIN: '관리자' } as const;

export function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
      navigate(LOGIN_PATH, { replace: true });
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4 sm:px-6">
          <div>
            <Link to={HOSPITALS_PATH} className="text-lg font-bold text-blue-700">MedFlow AI</Link>
            {user && <p className="text-xs text-slate-500">{user.email} · {ROLE_LABEL[user.role]}</p>}
          </div>
          <div className="flex items-center gap-2">
            <Link to={HOSPITALS_PATH} className="rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100">병원 찾기</Link>
            {user?.role === 'ADMIN' && (
              <Link to={ADMIN_USERS_PATH} className="rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100">사용자 관리</Link>
            )}
            {user?.role === 'PATIENT' && (
              <>
                <Link to={PATIENT_RESERVATIONS_PATH} className="rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100">내 예약</Link>
                <Link to={WITHDRAW_PATH} className="rounded-md px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50">회원 탈퇴</Link>
              </>
            )}
            <button
              type="button"
              onClick={handleLogout}
              disabled={isLoggingOut}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isLoggingOut ? '로그아웃 중...' : '로그아웃'}
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-10 sm:px-6"><Outlet /></main>
    </div>
  );
}