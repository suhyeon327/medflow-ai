import type { FormEvent } from 'react';
import { useHospitalsQuery } from '../../hospitals/hospitalQueries';
import type { DoctorSignupForm } from '../../../types/auth';

interface Props {
  form: DoctorSignupForm;
  onChange: (form: DoctorSignupForm) => void;
  onPrevious: () => void;
  onSubmit: () => void;
  isPending: boolean;
  errorMessage: string;
}

export function DoctorInfoStep({ form, onChange, onPrevious, onSubmit, isPending, errorMessage }: Props) {
  const hospitalsQuery = useHospitalsQuery('');
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => { event.preventDefault(); if (form.hospitalId) onSubmit(); };

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      <div><p className="text-sm font-semibold text-blue-700">의사 회원가입</p><h1 className="mt-2 text-2xl font-bold">의사 정보를 입력해 주세요.</h1><p className="mt-2 text-sm text-slate-600">가입 후 관리자 승인이 필요합니다.</p></div>
      <label className="block text-sm font-medium">이름<input required maxLength={50} value={form.name} onChange={(event) => onChange({ ...form, name: event.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
      <label className="block text-sm font-medium">소속 병원
        <select required value={form.hospitalId ?? ''} onChange={(event) => onChange({ ...form, hospitalId: event.target.value ? Number(event.target.value) : null })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5">
          <option value="">병원을 선택해 주세요</option>
          {hospitalsQuery.data?.map((hospital) => <option key={hospital.id} value={hospital.id}>{hospital.name} · {hospital.region}</option>)}
        </select>
        {hospitalsQuery.isPending && <span className="mt-1 block text-xs text-slate-500">병원 목록을 불러오고 있습니다.</span>}
        {hospitalsQuery.isError && <span className="mt-1 block text-xs text-red-600">병원 목록을 불러오지 못했습니다.</span>}
      </label>
      <label className="block text-sm font-medium">면허번호<input required maxLength={30} value={form.licenseNumber} onChange={(event) => onChange({ ...form, licenseNumber: event.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
      <label className="block text-sm font-medium">진료과<input required maxLength={100} value={form.specialty} onChange={(event) => onChange({ ...form, specialty: event.target.value })} placeholder="예: 내과" className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
      <label className="block text-sm font-medium">소개<textarea maxLength={1000} rows={4} value={form.introduction} onChange={(event) => onChange({ ...form, introduction: event.target.value })} className="mt-2 w-full resize-y rounded-md border border-slate-300 px-3 py-2.5" /></label>
      <label className="block text-sm font-medium">연락처<input type="tel" maxLength={20} value={form.contact} onChange={(event) => onChange({ ...form, contact: event.target.value })} placeholder="02-1234-5678" className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
      {errorMessage && <p role="alert" className="rounded-md bg-red-50 p-3 text-sm text-red-700">{errorMessage}</p>}
      <div className="flex gap-3"><button type="button" onClick={onPrevious} disabled={isPending} className="flex-1 rounded-md border border-slate-300 px-4 py-2.5 font-semibold text-slate-700 disabled:opacity-60">이전</button><button type="submit" disabled={isPending || hospitalsQuery.isPending || !form.hospitalId} className="flex-1 rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white hover:bg-blue-700 disabled:opacity-60">{isPending ? '신청 중...' : '가입 및 인증 신청'}</button></div>
    </form>
  );
}