import type { SignupRole } from '../../../types/auth';

export function SignupRoleStep({ onSelect }: { onSelect: (role: SignupRole) => void }) {
  return (
    <div>
      <h1 className="text-2xl font-bold">가입 유형을 선택해 주세요.</h1>
      <p className="mt-2 text-sm text-slate-600">이용할 서비스에 맞는 회원 유형을 선택합니다.</p>
      <div className="mt-7 grid gap-4 sm:grid-cols-2">
        <button type="button" onClick={() => onSelect('PATIENT')} className="rounded-xl border border-slate-200 p-6 text-left transition hover:border-blue-500 hover:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-500">
          <p className="font-bold text-blue-700">환자로 가입</p>
          <p className="mt-3 text-sm leading-6 text-slate-600">병원을 검색하고 진료를 예약할 수 있습니다.</p>
        </button>
        <button type="button" onClick={() => onSelect('DOCTOR')} className="rounded-xl border border-slate-200 p-6 text-left transition hover:border-blue-500 hover:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-500">
          <p className="font-bold text-blue-700">의사로 가입</p>
          <p className="mt-3 text-sm leading-6 text-slate-600">예약과 환자 문진을 관리할 수 있습니다.</p>
          <p className="mt-2 text-xs font-semibold text-amber-700">가입 후 관리자 승인이 필요합니다.</p>
        </button>
      </div>
    </div>
  );
}