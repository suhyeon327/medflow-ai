import { useAuth } from "../auth/AuthContext";

const ROLE_DESCRIPTION = {
  PATIENT: "환자 전용 서비스의 공통 인증 기반이 준비되었습니다.",
  DOCTOR: "의사 전용 서비스의 공통 인증 기반이 준비되었습니다.",
  ADMIN: "관리자 전용 서비스의 공통 인증 기반이 준비되었습니다.",
} as const;

export function RoleHomePage() {
  const { user } = useAuth();
  if (!user) return null;

  return (
    <section className="rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
      <p className="text-sm font-semibold text-blue-700">{user.role}</p>
      <h1 className="mt-2 text-2xl font-bold">Medflow에 로그인했습니다.</h1>
      <p className="mt-3 text-slate-600">{ROLE_DESCRIPTION[user.role]}</p>
    </section>
  );
}
