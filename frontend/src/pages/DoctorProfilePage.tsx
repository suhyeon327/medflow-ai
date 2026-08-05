import { useEffect, useState, type FormEvent } from 'react';
import { getApiErrorMessage } from '../api/apiError';
import { LoadingScreen } from '../components/LoadingScreen';
import { QueryError } from '../components/QueryError';
import { useDoctorProfileQuery, useUpdateDoctorProfileMutation } from '../features/doctors/doctorQueries';
import { useHospitalsQuery } from '../features/hospitals/hospitalQueries';
import type { DoctorProfileUpdateRequest } from '../types/doctor';

const EMPTY_FORM: DoctorProfileUpdateRequest = { hospitalId: 0, name: '', licenseNumber: '', specialty: '', introduction: '', contact: '' };
const STATUS_LABEL = { PENDING: '승인 대기', ACTIVE: '승인 완료', REJECTED: '승인 거절' } as const;

export function DoctorProfilePage() {
  const profileQuery = useDoctorProfileQuery();
  const hospitalsQuery = useHospitalsQuery('');
  const updateMutation = useUpdateDoctorProfileMutation();
  const [form, setForm] = useState(EMPTY_FORM);

  useEffect(() => {
    if (!profileQuery.data) return;
    const profile = profileQuery.data;
    setForm({ hospitalId: profile.hospitalId, name: profile.doctorName, licenseNumber: profile.licenseNumber, specialty: profile.specialty ?? '', introduction: profile.introduction ?? '', contact: profile.contact ?? '' });
  }, [profileQuery.data]);

  if (profileQuery.isPending) return <LoadingScreen />;
  if (profileQuery.isError) return <QueryError error={profileQuery.error} onRetry={() => profileQuery.refetch()} />;

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    updateMutation.mutate({ ...form, name: form.name.trim(), licenseNumber: form.licenseNumber.trim(), specialty: form.specialty.trim(), introduction: form.introduction.trim(), contact: form.contact.trim() });
  };

  return (
    <section className="mx-auto max-w-2xl rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
      <div className="flex items-start justify-between gap-4"><div><p className="text-sm font-semibold text-blue-700">의사 정보</p><h1 className="mt-2 text-2xl font-bold">내 프로필</h1></div><span className="rounded-full bg-slate-100 px-3 py-1 text-sm font-semibold">{STATUS_LABEL[profileQuery.data.status]}</span></div>
      <form className="mt-8 space-y-5" onSubmit={submit}>
        <label className="block text-sm font-medium text-slate-700">이름<input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
        <label className="block text-sm font-medium text-slate-700">소속 병원<select required value={form.hospitalId || ''} onChange={(e) => setForm({ ...form, hospitalId: Number(e.target.value) })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5"><option value="">병원 선택</option>{hospitalsQuery.data?.map((hospital) => <option key={hospital.id} value={hospital.id}>{hospital.name} · {hospital.region}</option>)}</select></label>
        {hospitalsQuery.isError && <p className="text-sm text-red-700">병원 목록을 불러오지 못했습니다.</p>}
        <label className="block text-sm font-medium text-slate-700">면허번호<input required value={form.licenseNumber} onChange={(e) => setForm({ ...form, licenseNumber: e.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
        <label className="block text-sm font-medium text-slate-700">진료과<input maxLength={100} value={form.specialty} onChange={(e) => setForm({ ...form, specialty: e.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
        <label className="block text-sm font-medium text-slate-700">소개<textarea rows={5} maxLength={1000} value={form.introduction} onChange={(e) => setForm({ ...form, introduction: e.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
        <label className="block text-sm font-medium text-slate-700">연락처<input maxLength={20} value={form.contact} onChange={(e) => setForm({ ...form, contact: e.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5" /></label>
        {updateMutation.isError && <p role="alert" className="rounded-md bg-red-50 p-3 text-sm text-red-700">{getApiErrorMessage(updateMutation.error)}</p>}
        {updateMutation.isSuccess && <p role="status" className="rounded-md bg-green-50 p-3 text-sm text-green-700">프로필이 수정되었습니다.</p>}
        <button disabled={updateMutation.isPending || hospitalsQuery.isPending} className="w-full rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white disabled:opacity-60">{updateMutation.isPending ? '저장 중...' : '프로필 저장'}</button>
      </form>
    </section>
  );
}