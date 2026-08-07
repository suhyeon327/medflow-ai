import { useState } from "react";
import { Link, Outlet, useNavigate } from "react-router";
import { useAuth } from "../auth/AuthContext";
import {
  HOSPITALS_PATH,
  LOGIN_PATH,
  PATIENT_PROFILE_PATH,
  PATIENT_RESERVATIONS_PATH,
  ROLE_HOME_PATH,
  SIGNUP_PATH,
} from "../routes/routePaths";

export function PublicLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
      navigate(HOSPITALS_PATH, { replace: true });
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
          <Link
            to={HOSPITALS_PATH}
            className="text-2xl font-extrabold tracking-tight text-blue-600"
          >
            ✚ Medflow
          </Link>
          <nav className="flex items-center gap-2" aria-label="주요 메뉴">
            {user?.role === "PATIENT" ? (
              <>
                <Link
                  to={PATIENT_RESERVATIONS_PATH}
                  className="px-5 py-2 text-sm font-bold text-slate-800 hover:text-blue-600"
                >
                  내 예약
                </Link>
                <Link
                  to={PATIENT_PROFILE_PATH}
                  className="px-5 py-2 text-sm font-bold text-slate-800 hover:text-blue-600"
                >
                  내 프로필
                </Link>
                <button
                  type="button"
                  onClick={handleLogout}
                  disabled={isLoggingOut}
                  className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-bold text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                >
                  {isLoggingOut ? "로그아웃 중..." : "로그아웃"}
                </button>
              </>
            ) : user ? (
              <Link
                to={ROLE_HOME_PATH[user.role]}
                className="rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white hover:bg-blue-700"
              >
                내 화면
              </Link>
            ) : (
              <>
                <Link
                  to={LOGIN_PATH}
                  className="rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-bold text-white shadow-sm hover:bg-blue-700"
                >
                  로그인
                </Link>
                <Link
                  to={SIGNUP_PATH}
                  className="rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-bold text-white shadow-sm hover:bg-blue-700"
                >
                  회원가입
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}
