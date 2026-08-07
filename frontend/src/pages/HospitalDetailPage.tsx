import { Link, useParams } from "react-router";
import { QueryError } from "../components/QueryError";
import {
  useHospitalDoctorsQuery,
  useHospitalQuery,
} from "../features/hospitals/hospitalQueries";
import { DOCTOR_DETAIL_PATH, HOSPITALS_PATH } from "../routes/routePaths";

export function HospitalDetailPage() {
  const hospitalId = Number(useParams().hospitalId);
  const isValidId = Number.isInteger(hospitalId) && hospitalId > 0;
  const hospitalQuery = useHospitalQuery(hospitalId);
  const doctorsQuery = useHospitalDoctorsQuery(hospitalId);
  const specialties = [
    ...new Set(
      (doctorsQuery.data ?? [])
        .map((doctor) => doctor.specialty)
        .filter(Boolean),
    ),
  ];

  if (!isValidId)
    return <QueryError error={new Error("올바르지 않은 병원 번호입니다.")} />;

  return (
    <section className="mx-auto max-w-7xl px-6 py-10">
      <Link to={HOSPITALS_PATH} className="text-sm font-bold text-blue-600">
        ← 병원 목록
      </Link>
      {hospitalQuery.isPending && (
        <p role="status" className="mt-8 text-sm text-slate-600">
          병원 정보를 불러오고 있습니다.
        </p>
      )}
      {hospitalQuery.isError && (
        <div className="mt-8">
          <QueryError
            error={hospitalQuery.error}
            onRetry={() => hospitalQuery.refetch()}
          />
        </div>
      )}
      {hospitalQuery.data && (
        <div className="mt-8 rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
          <p className="font-bold text-blue-600">{hospitalQuery.data.region}</p>
          <h1 className="mt-3 text-4xl font-extrabold">
            {hospitalQuery.data.name}
          </h1>
          <div className="mt-7 flex flex-wrap items-center gap-6 text-slate-700">
            <p>
              <span className="mr-2 text-blue-600">☎</span>
              {hospitalQuery.data.tel}
            </p>
            <p>
              <span className="mr-2 text-blue-600">♙</span>진료 의사{" "}
              {doctorsQuery.data?.length ?? 0}명
            </p>
          </div>
          {specialties.length > 0 && (
            <div className="mt-6 flex flex-wrap gap-2">
              {specialties.map((specialty) => (
                <span
                  key={specialty}
                  className="rounded-full bg-blue-50 px-4 py-2 text-sm font-semibold text-blue-700"
                >
                  {specialty}
                </span>
              ))}
            </div>
          )}
        </div>
      )}

      <section className="mt-10">
        <h2 className="text-2xl font-extrabold">진료 의사</h2>
        {doctorsQuery.isPending && (
          <p role="status" className="mt-6 text-sm text-slate-600">
            의사 목록을 불러오고 있습니다.
          </p>
        )}
        {doctorsQuery.isError && (
          <div className="mt-6">
            <QueryError
              error={doctorsQuery.error}
              onRetry={() => doctorsQuery.refetch()}
            />
          </div>
        )}
        {doctorsQuery.isSuccess && doctorsQuery.data.length === 0 && (
          <p className="mt-6 rounded-2xl border border-slate-200 bg-white p-10 text-center text-slate-600">
            현재 조회 가능한 의사가 없습니다.
          </p>
        )}
        {doctorsQuery.data && doctorsQuery.data.length > 0 && (
          <ul className="mt-6 grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {doctorsQuery.data.map((doctor) => (
              <li
                key={doctor.doctorId}
                className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
              >
                <p className="text-sm font-bold text-blue-600">
                  {doctor.specialty || "진료과 정보 없음"}
                </p>
                <h3 className="mt-2 text-xl font-extrabold">
                  {doctor.doctorName} 의사
                </h3>
                <p className="mt-3 text-sm font-semibold text-slate-700">
                  연락처 {doctor.contact}
                </p>
                <p className="mt-3 line-clamp-2 min-h-10 text-sm leading-5 text-slate-500">
                  {doctor.introduction || "등록된 소개가 없습니다."}
                </p>
                <Link
                  to={DOCTOR_DETAIL_PATH(doctor.doctorId)}
                  className="mt-6 block rounded-lg border border-blue-200 py-2.5 text-center text-sm font-bold text-blue-600 hover:bg-blue-50"
                >
                  상세 정보 보기
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </section>
  );
}
