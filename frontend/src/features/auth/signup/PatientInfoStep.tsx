import type { FormEvent } from 'react';
import type { PatientSignupForm } from '../../../types/auth';

interface Props {
  form: PatientSignupForm;
  onChange: (form: PatientSignupForm) => void;
  onPrevious: () => void;
  onSubmit: () => void;
  isPending: boolean;
  errorMessage: string;
}

export function PatientInfoStep({ form, onChange, onPrevious, onSubmit, isPending, errorMessage }: Props) {
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => { event.preventDefault(); onSubmit(); };
  const today = new Date().toISOString().slice(0, 10);

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      <div><p className="text-sm font-semibold text-blue-700">환자 회원가입</p><h1 className="mt-2 text-2xl font-bold">환자 정보를 입력해 주세요.</h1></div>
      <label className="block text-sm font-medium">이름<input required maxLength={50} value={form.name} onChange={(event) => onChange({ ...form, name: event.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
      <label className="block text-sm font-medium">생년월일<input type="date" required max={today} value={form.birth} onChange={(event) => onChange({ ...form, birth: event.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
      <fieldset><legend className="text-sm font-medium">성별</legend><div className="mt-2 flex gap-5"><label className="flex items-center gap-2 text-sm"><input type="radio" name="gender" checked={form.gender === 'MALE'} onChange={() => onChange({ ...form, gender: 'MALE' })} /> 남성</label><label className="flex items-center gap-2 text-sm"><input type="radio" name="gender" checked={form.gender === 'FEMALE'} onChange={() => onChange({ ...form, gender: 'FEMALE' })} /> 여성</label></div></fieldset>
      <label className="block text-sm font-medium">전화번호<input type="tel" required pattern="\d{3}-?\d{3,4}-?\d{4}" value={form.phone} onChange={(event) => onChange({ ...form, phone: event.target.value })} placeholder="010-1234-5678" className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
      {errorMessage && <p role="alert" className="rounded-md bg-red-50 p-3 text-sm text-red-700">{errorMessage}</p>}
      <div className="flex gap-3"><button type="button" onClick={onPrevious} disabled={isPending} className="flex-1 rounded-md border border-slate-300 px-4 py-2.5 font-semibold text-slate-700 disabled:opacity-60">이전</button><button type="submit" disabled={isPending} className="flex-1 rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white hover:bg-blue-700 disabled:opacity-60">{isPending ? '가입 중...' : '회원가입'}</button></div>
    </form>
  );
}