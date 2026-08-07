import { useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router";
import { useAuth } from "../auth/AuthContext";
import { useDoctorProfileQuery } from "../features/doctors/doctorQueries";
import {
  ADMIN_DOCTORS_PATH,
  ADMIN_HOSPITALS_PATH,
  ADMIN_RESERVATIONS_PATH,
  ADMIN_USERS_PATH,
  DOCTOR_PROFILE_PATH,
  DOCTOR_RESERVATIONS_PATH,
  HOSPITALS_PATH,
  PATIENT_PROFILE_PATH,
  PATIENT_RESERVATIONS_PATH,
  ROLE_HOME_PATH,
} from "../routes/routePaths";

export function AppLayout() {
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

  if (user?.role === "DOCTOR")
    return <DoctorLayout onLogout={handleLogout} isLoggingOut={isLoggingOut} />;
  if (user?.role === "ADMIN")
    return <AdminLayout onLogout={handleLogout} isLoggingOut={isLoggingOut} />;

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
          <div className="flex items-center gap-2">
            {user?.role !== "PATIENT" && (
              <Link
                to={HOSPITALS_PATH}
                className="rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
              >
                병원 찾기
              </Link>
            )}
            {user?.role === "PATIENT" && (
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
              </>
            )}
            <button
              type="button"
              onClick={handleLogout}
              disabled={isLoggingOut}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isLoggingOut ? "로그아웃 중..." : "로그아웃"}
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-6 py-10">
        <Outlet />
      </main>
    </div>
  );
}

function AdminLayout({
  onLogout,
  isLoggingOut,
}: {
  onLogout: () => Promise<void>;
  isLoggingOut: boolean;
}) {
  const links = [
    { to: ROLE_HOME_PATH.ADMIN, label: "대시보드", icon: "⌂", end: true },
    { to: ADMIN_HOSPITALS_PATH, label: "병원 관리", icon: "▦" },
    { to: ADMIN_RESERVATIONS_PATH, label: "예약 관리", icon: "▣" },
    { to: ADMIN_USERS_PATH, label: "사용자 관리", icon: "♙" },
    { to: ADMIN_DOCTORS_PATH, label: "의사 관리", icon: "✚" },
  ];

  return (
    <div className="min-h-screen bg-slate-50 lg:flex">
      <aside className="border-b border-slate-200 bg-white lg:fixed lg:inset-y-0 lg:w-64 lg:border-b-0 lg:border-r">
        <div className="flex h-full flex-col p-4">
          <Link
            to={ROLE_HOME_PATH.ADMIN}
            className="px-3 py-5 text-2xl font-extrabold text-blue-600"
          >
            ✚ Medflow
          </Link>
          <nav className="mt-5 grid grid-cols-2 gap-2 sm:grid-cols-5 lg:grid-cols-1">
            {links.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-xl px-4 py-3.5 text-sm font-bold transition ${isActive ? "bg-blue-50 text-blue-700" : "text-slate-700 hover:bg-slate-50"}`
                }
              >
                <span className="text-lg">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </nav>
          <button
            type="button"
            onClick={onLogout}
            disabled={isLoggingOut}
            className="mt-auto hidden w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-bold text-slate-700 hover:bg-slate-50 disabled:opacity-50 lg:block"
          >
            {isLoggingOut ? "로그아웃 중..." : "로그아웃"}
          </button>
        </div>
      </aside>
      <main className="min-w-0 flex-1 px-4 py-8 sm:px-7 lg:ml-64 lg:px-10">
        <div className="mx-auto max-w-7xl">
          <Outlet />
        </div>
      </main>
    </div>
  );
}

function DoctorLayout({
  onLogout,
  isLoggingOut,
}: {
  onLogout: () => Promise<void>;
  isLoggingOut: boolean;
}) {
  const profileQuery = useDoctorProfileQuery();
  const links = [
    { to: ROLE_HOME_PATH.DOCTOR, label: "대시보드", icon: "⌂", end: true },
    { to: DOCTOR_RESERVATIONS_PATH, label: "진료 관리", icon: "▣" },
    { to: DOCTOR_PROFILE_PATH, label: "내 프로필", icon: "♙" },
  ];

  return (
    <div className="min-h-screen bg-slate-50 lg:flex">
      <aside className="border-b border-slate-200 bg-white lg:fixed lg:inset-y-0 lg:w-60 lg:border-b-0 lg:border-r">
        <div className="flex h-full flex-col p-4">
          <Link
            to={ROLE_HOME_PATH.DOCTOR}
            className="px-3 py-4 text-xl font-bold text-blue-700"
          >
            ✚ Medflow
          </Link>
          <nav className="mt-3 grid grid-cols-2 gap-2 sm:grid-cols-5 lg:grid-cols-1">
            {links.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition ${isActive ? "bg-blue-50 text-blue-700" : "text-slate-700 hover:bg-slate-50"}`
                }
              >
                <span className="text-lg">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </nav>
          <div className="mt-auto hidden lg:block">
            <div className="rounded-xl border border-slate-200 p-4">
              <p className="font-bold">
                {profileQuery.data?.doctorName ?? "의사"} 의사
              </p>
              <p className="mt-1 text-sm text-slate-500">
                {profileQuery.data?.hospitalName ?? ""}
              </p>
              <p className="mt-1 text-xs text-slate-400">
                {profileQuery.data?.specialty ?? "진료과 미등록"}
              </p>
            </div>
            <button
              type="button"
              onClick={onLogout}
              disabled={isLoggingOut}
              className="mt-3 w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-50"
            >
              {isLoggingOut ? "로그아웃 중..." : "로그아웃"}
            </button>
          </div>
        </div>
      </aside>
      <main className="min-w-0 flex-1 px-4 py-8 sm:px-7 lg:ml-60 lg:px-10">
        <div className="mx-auto max-w-7xl">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
