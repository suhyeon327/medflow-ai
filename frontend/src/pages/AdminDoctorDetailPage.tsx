import { useParams } from 'react-router-dom';
import { getApiErrorMessage } from '../api/apiError';
import { QueryError } from '../components/QueryError';
import { useAdminDoctorQuery, useDoctorDecisionMutation } from '../features/doctors/doctorQueries';

const STATUS_LABEL = { PENDING: '승인 대기', ACTIVE: '승인 완료', REJECTED: '승인 거절' } as const;

export function AdminDoctorDetailPage() {
  const doctorId = Number(useParams().doctorId);
  const doctorQuery = useAdminDoctorQuery(doctorId);
  const decisionMutation = useDoctorDecisionMutation(doctorId);

  if (!Number.isInteger(doctorId) || doctorId <= 0) return <QueryError error={new Error('올바르지 않은 의사 번호입니다.')} />;
  if (doctorQuery.isPending) return <p role="status" className="text-sm text-slate-600">의사 정보를 불러오고 있습니다.</p>;
  if (doctorQuery.isError) return <QueryError error={doctorQuery.error} onRetry={() => doctorQuery.refetch()} />;

  const doctor = doctorQuery.data;
  return (
    <section className="mx-auto max-w-2xl rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
      <div className="flex items-start justify-between gap-4"><div><p className="text-sm font-semibold text-blue-700">의사 인증 관리</p><h1 className="mt-2 text-2xl font-bold">{doctor.doctorName} 의사</h1></div><span className="rounded-full bg-slate-100 px-3 py-1 text-sm font-semibold">{STATUS_LABEL[doctor.status]}</span></div>
      <dl className="mt-8 grid gap-5 sm:grid-cols-2">
        <div><dt className="text-sm text-slate-500">이메일</dt><dd className="mt-1 font-semibold">{doctor.email}</dd></div>
        <div><dt className="text-sm text-slate-500">면허번호</dt><dd className="mt-1 font-semibold">{doctor.licenseNumber}</dd></div>
        <div><dt className="text-sm text-slate-500">소속 병원</dt><dd className="mt-1 font-semibold">{doctor.hospitalName}</dd></div>
        <div><dt className="text-sm text-slate-500">진료과</dt><dd className="mt-1 font-semibold">{doctor.specialty || '-'}</dd></div>
        <div><dt className="text-sm text-slate-500">연락처</dt><dd className="mt-1 font-semibold">{doctor.contact || '-'}</dd></div>
      </dl>
      <div className="mt-7 border-t border-slate-200 pt-6"><h2 className="font-bold">소개</h2><p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-slate-600">{doctor.introduction || '등록된 소개가 없습니다.'}</p></div>
      {decisionMutation.isError && <p role="alert" className="mt-5 rounded-md bg-red-50 p-3 text-sm text-red-700">{getApiErrorMessage(decisionMutation.error)}</p>}
      {decisionMutation.isSuccess && <p role="status" className="mt-5 rounded-md bg-green-50 p-3 text-sm text-green-700">{decisionMutation.data.message}</p>}
      {doctor.status === 'PENDING' && <div className="mt-7 flex gap-3"><button type="button" disabled={decisionMutation.isPending} onClick={() => decisionMutation.mutate('reject')} className="flex-1 rounded-md border border-red-300 px-4 py-2.5 font-semibold text-red-700 disabled:opacity-60">거절</button><button type="button" disabled={decisionMutation.isPending} onClick={() => decisionMutation.mutate('approve')} className="flex-1 rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white disabled:opacity-60">승인</button></div>}
    </section>
  );
}