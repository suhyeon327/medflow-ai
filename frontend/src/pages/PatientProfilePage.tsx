import { useEffect, useState, type FormEvent } from 'react';
import { getApiErrorMessage } from '../api/apiError';
import { LoadingScreen } from '../components/LoadingScreen';
import { QueryError } from '../components/QueryError';
import { usePatientProfileQuery, useUpdatePatientProfileMutation } from '../features/patients/patientProfileQueries';
import type { PatientProfileUpdateRequest } from '../types/patient';

const EMPTY_FORM: PatientProfileUpdateRequest = { name: '', birth: '', gender: 'MALE', phone: '' };

export function PatientProfilePage() {
  const profileQuery = usePatientProfileQuery();
  const updateMutation = useUpdatePatientProfileMutation();
  const [form, setForm] = useState(EMPTY_FORM);
  const [validationMessage, setValidationMessage] = useState('');

  useEffect(() => {
    if (!profileQuery.data) return;
    const { name, birth, gender, phone } = profileQuery.data;
    setForm({ name, birth, gender, phone });
  }, [profileQuery.data]);

  if (profileQuery.isPending) return <LoadingScreen />;
  if (profileQuery.isError) return <QueryError error={profileQuery.error} onRetry={() => profileQuery.refetch()} />;

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const request = { ...form, name: form.name.trim(), phone: form.phone.trim() };

    if (!request.name) {
      setValidationMessage('이름을 입력해 주세요.');
      return;
    }
    if (!/^\d{10,11}$/.test(request.phone)) {
      setValidationMessage('전화번호는 하이픈 없이 10~11자리 숫자로 입력해 주세요.');
      return;
    }

    setValidationMessage('');
    updateMutation.mutate(request);
  };

  return (
    <section className="mx-auto max-w-2xl rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
      <p className="text-sm font-semibold text-blue-700">환자 정보</p>
      <h1 className="mt-2 text-2xl font-bold">내 프로필</h1>
      <p className="mt-2 text-sm text-slate-600">진료 예약에 사용되는 본인 정보를 확인하고 수정할 수 있습니다.</p>

      <form className="mt-8 space-y-5" onSubmit={handleSubmit}>
        <div>
          <label htmlFor="patientName" className="mb-2 block text-sm font-medium text-slate-700">이름</label>
          <input id="patientName" required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" />
        </div>
        <div>
          <label htmlFor="patientBirth" className="mb-2 block text-sm font-medium text-slate-700">생년월일</label>
          <input id="patientBirth" type="date" required value={form.birth} onChange={(event) => setForm({ ...form, birth: event.target.value })} className="w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" />
        </div>
        <fieldset>
          <legend className="mb-2 text-sm font-medium text-slate-700">성별</legend>
          <div className="flex gap-5">
            <label className="flex items-center gap-2 text-sm"><input type="radio" name="gender" value="MALE" checked={form.gender === 'MALE'} onChange={() => setForm({ ...form, gender: 'MALE' })} />남성</label>
            <label className="flex items-center gap-2 text-sm"><input type="radio" name="gender" value="FEMALE" checked={form.gender === 'FEMALE'} onChange={() => setForm({ ...form, gender: 'FEMALE' })} />여성</label>
          </div>
        </fieldset>
        <div>
          <label htmlFor="patientPhone" className="mb-2 block text-sm font-medium text-slate-700">전화번호</label>
          <input id="patientPhone" type="tel" inputMode="numeric" required pattern="[0-9]{10,11}" placeholder="01012345678" value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} className="w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" />
          <p className="mt-1 text-xs text-slate-500">하이픈 없이 숫자만 입력해 주세요.</p>
        </div>
        {(validationMessage || updateMutation.isError) && <p role="alert" className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">{validationMessage || getApiErrorMessage(updateMutation.error)}</p>}
        {updateMutation.isSuccess && <p role="status" className="rounded-md bg-green-50 px-3 py-2 text-sm text-green-700">프로필이 수정되었습니다.</p>}
        <button type="submit" disabled={updateMutation.isPending} className="w-full rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60">{updateMutation.isPending ? '저장 중...' : '프로필 저장'}</button>
      </form>
    </section>
  );
}