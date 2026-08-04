import { Link, useParams } from 'react-router-dom';
import { QueryError } from '../components/QueryError';
import { useDoctorQuery } from '../features/doctors/doctorQueries';
import { HOSPITAL_DETAIL_PATH } from '../routes/routePaths';

export function DoctorDetailPage() {
  const doctorId = Number(useParams().doctorId);
  const isValidId = Number.isInteger(doctorId) && doctorId > 0;
  const doctorQuery = useDoctorQuery(doctorId);

  if (!isValidId) {
    return <QueryError error={new Error('올바르지 않은 의사 번호입니다.')} />;
  }

  if (doctorQuery.isPending) {
    return <p role="status" className="text-sm text-slate-600">의사 정보를 불러오고 있습니다.</p>;
  }

  if (doctorQuery.isError) {
    return <QueryError error={doctorQuery.error} onRetry={() => doctorQuery.refetch()} />;
  }

  return (
    <section className="mx-auto max-w-2xl">
      <Link to={HOSPITAL_DETAIL_PATH(doctorQuery.data.hospitalId)} className="text-sm font-semibold text-blue-700">← 병원 상세</Link>
      <div className="mt-6 rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
        <p className="text-sm font-semibold text-blue-700">{doctorQuery.data.specialty || '진료과 정보 없음'}</p>
        <h1 className="mt-2 text-3xl font-bold">{doctorQuery.data.doctorName} 의사</h1>

        <dl className="mt-7 grid gap-5 border-t border-slate-200 pt-6 sm:grid-cols-2">
          <div>
            <dt className="text-sm font-medium text-slate-500">소속 병원</dt>
            <dd className="mt-1 font-semibold text-slate-900">{doctorQuery.data.hospitalName}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-slate-500">연락처</dt>
            <dd className="mt-1 font-semibold text-slate-900">{doctorQuery.data.contact}</dd>
          </div>
        </dl>

        <div className="mt-7 border-t border-slate-200 pt-6">
          <h2 className="font-bold">의사 소개</h2>
          <p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-slate-600">
            {doctorQuery.data.introduction || '등록된 소개가 없습니다.'}
          </p>
        </div>
      </div>
    </section>
  );
}