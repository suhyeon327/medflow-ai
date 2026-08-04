import { Link, useParams } from 'react-router-dom';
import { QueryError } from '../components/QueryError';
import { useHospitalDoctorsQuery, useHospitalQuery } from '../features/hospitals/hospitalQueries';
import { DOCTOR_DETAIL_PATH, HOSPITALS_PATH } from '../routes/routePaths';

export function HospitalDetailPage() {
  const hospitalId = Number(useParams().hospitalId);
  const isValidId = Number.isInteger(hospitalId) && hospitalId > 0;
  const hospitalQuery = useHospitalQuery(hospitalId);
  const doctorsQuery = useHospitalDoctorsQuery(hospitalId);

  if (!isValidId) {
    return <QueryError error={new Error('올바르지 않은 병원 번호입니다.')} />;
  }

  return (
    <section>
      <Link to={HOSPITALS_PATH} className="text-sm font-semibold text-blue-700">← 병원 목록</Link>

      <div className="mt-6">
        {hospitalQuery.isPending && <p role="status" className="text-sm text-slate-600">병원 정보를 불러오고 있습니다.</p>}
        {hospitalQuery.isError && <QueryError error={hospitalQuery.error} onRetry={() => hospitalQuery.refetch()} />}
        {hospitalQuery.data && (
          <div className="rounded-xl border border-slate-200 bg-white p-7 shadow-sm">
            <p className="text-sm font-semibold text-blue-700">{hospitalQuery.data.region}</p>
            <h1 className="mt-2 text-3xl font-bold">{hospitalQuery.data.name}</h1>
            <dl className="mt-6 grid gap-4 text-sm sm:grid-cols-2">
              <div><dt className="font-medium text-slate-500">주소</dt><dd className="mt-1 text-slate-900">{hospitalQuery.data.address}</dd></div>
              <div><dt className="font-medium text-slate-500">전화번호</dt><dd className="mt-1 text-slate-900">{hospitalQuery.data.tel}</dd></div>
            </dl>
          </div>
        )}
      </div>

      <div className="mt-10">
        <h2 className="text-xl font-bold">진료 의사</h2>
        <p className="mt-2 text-sm text-slate-600">승인 완료된 의사만 표시됩니다.</p>
        <div className="mt-5">
          {doctorsQuery.isPending && <p role="status" className="text-sm text-slate-600">의사 목록을 불러오고 있습니다.</p>}
          {doctorsQuery.isError && <QueryError error={doctorsQuery.error} onRetry={() => doctorsQuery.refetch()} />}
          {doctorsQuery.isSuccess && doctorsQuery.data.length === 0 && <p className="rounded-lg border border-slate-200 bg-white p-6 text-slate-600">현재 조회 가능한 의사가 없습니다.</p>}
          {doctorsQuery.data && doctorsQuery.data.length > 0 && (
            <ul className="grid gap-4 sm:grid-cols-2">
              {doctorsQuery.data.map((doctor) => (
                <li key={doctor.doctorId}>
                  <Link to={DOCTOR_DETAIL_PATH(doctor.doctorId)} className="block rounded-lg border border-slate-200 bg-white p-5 hover:border-blue-300">
                    <p className="font-bold">{doctor.doctorName} 의사</p>
                    <p className="mt-1 text-sm text-slate-600">{doctor.specialty || '진료과 정보 없음'}</p>
                    <p className="mt-3 text-sm text-slate-500">연락처 {doctor.contact}</p>
                    <p className="mt-3 text-sm font-medium text-blue-700">상세 정보 보기</p>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </section>
  );
}