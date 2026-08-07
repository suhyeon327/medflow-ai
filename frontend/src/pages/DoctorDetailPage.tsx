import { Link, useParams } from "react-router";
import { QueryError } from "../components/QueryError";
import { useDoctorQuery } from "../features/doctors/doctorQueries";
import { ScheduleBookingSection } from "../features/reservations/ScheduleBookingSection";
import { HOSPITAL_DETAIL_PATH } from "../routes/routePaths";

export function DoctorDetailPage() {
  const doctorId = Number(useParams().doctorId);
  const isValidId = Number.isInteger(doctorId) && doctorId > 0;
  const doctorQuery = useDoctorQuery(doctorId);

  if (!isValidId)
    return <QueryError error={new Error("올바르지 않은 의사 번호입니다.")} />;
  if (doctorQuery.isPending)
    return (
      <p
        role="status"
        className="mx-auto max-w-7xl px-6 py-10 text-sm text-slate-600"
      >
        의사 정보를 불러오고 있습니다.
      </p>
    );
  if (doctorQuery.isError)
    return (
      <div className="mx-auto max-w-7xl px-6 py-10">
        <QueryError
          error={doctorQuery.error}
          onRetry={() => doctorQuery.refetch()}
        />
      </div>
    );

  return (
    <section className="mx-auto max-w-7xl px-6 py-10">
      <Link
        to={HOSPITAL_DETAIL_PATH(doctorQuery.data.hospitalId)}
        className="text-sm font-bold text-blue-600"
      >
        ← 병원 상세
      </Link>
      <div className="mt-8 grid gap-8 rounded-2xl border border-slate-200 bg-white p-8 shadow-sm lg:grid-cols-[1fr_1.4fr]">
        <div>
          <p className="text-sm font-bold text-blue-600">
            {doctorQuery.data.specialty || "진료과 정보 없음"}
          </p>
          <h1 className="mt-3 text-4xl font-extrabold">
            {doctorQuery.data.doctorName} 의사
          </h1>
          <dl className="mt-8 grid gap-5 sm:grid-cols-2">
            <div>
              <dt className="text-sm text-slate-500">소속 병원</dt>
              <dd className="mt-1 font-bold">
                {doctorQuery.data.hospitalName}
              </dd>
            </div>
            <div>
              <dt className="text-sm text-slate-500">진료 분야</dt>
              <dd className="mt-1 font-bold">
                {doctorQuery.data.specialty || "미등록"}
              </dd>
            </div>
            <div>
              <dt className="text-sm text-slate-500">연락처</dt>
              <dd className="mt-1 font-bold">{doctorQuery.data.contact}</dd>
            </div>
          </dl>
        </div>
        <div className="rounded-xl bg-slate-50 p-7">
          <h2 className="text-xl font-extrabold">의사 소개</h2>
          <p className="mt-5 whitespace-pre-wrap leading-7 text-slate-600">
            {doctorQuery.data.introduction || "등록된 소개가 없습니다."}
          </p>
        </div>
      </div>
      <ScheduleBookingSection doctorId={doctorId} />
    </section>
  );
}
