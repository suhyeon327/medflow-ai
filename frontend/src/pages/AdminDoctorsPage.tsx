import { useState } from "react";
import { Link } from "react-router";
import { QueryError } from "../components/QueryError";
import { useAdminDoctorsQuery } from "../features/doctors/doctorQueries";
import { ADMIN_DOCTOR_DETAIL_PATH } from "../routes/routePaths";
import type { DoctorStatus } from "../types/doctor";

const STATUS_LABEL: Record<DoctorStatus, string> = {
  PENDING: "승인 대기",
  ACTIVE: "승인 완료",
  REJECTED: "승인 거절",
};

export function AdminDoctorsPage() {
  const [status, setStatus] = useState<DoctorStatus | undefined>("PENDING");
  const doctorsQuery = useAdminDoctorsQuery(status);

  return (
    <section>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <h1 className="text-3xl font-bold">의사 관리</h1>
        <label className="text-sm font-medium text-slate-700">
          승인 상태
          <select
            value={status ?? ""}
            onChange={(event) =>
              setStatus(
                (event.target.value || undefined) as DoctorStatus | undefined,
              )
            }
            className="ml-3 rounded-lg border border-slate-300 px-4 py-2.5"
          >
            <option value="">전체</option>
            {Object.entries(STATUS_LABEL).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>
      </div>
      {doctorsQuery.isPending && (
        <p className="mt-6 text-sm text-slate-600">
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
      {doctorsQuery.data?.length === 0 && (
        <p className="mt-6 rounded-xl border border-slate-200 bg-white p-8 text-center text-slate-600">
          해당 상태의 의사가 없습니다.
        </p>
      )}
      {doctorsQuery.data && doctorsQuery.data.length > 0 && (
        <div className="mt-6 overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600">
              <tr>
                <th className="px-5 py-4">의사</th>
                <th className="px-5 py-4">소속 병원</th>
                <th className="px-5 py-4">면허번호</th>
                <th className="px-5 py-4">상태</th>
                <th className="px-5 py-4 text-right">관리</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {doctorsQuery.data.map((doctor) => (
                <tr key={doctor.doctorId} className="hover:bg-slate-50">
                  <td className="px-5 py-4 font-bold">
                    {doctor.doctorName} 의사
                  </td>
                  <td className="px-5 py-4">{doctor.hospitalName}</td>
                  <td className="px-5 py-4 text-slate-600">
                    {doctor.licenseNumber}
                  </td>
                  <td className="px-5 py-4">
                    <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold">
                      {STATUS_LABEL[doctor.status]}
                    </span>
                  </td>
                  <td className="px-5 py-4 text-right">
                    <Link
                      to={ADMIN_DOCTOR_DETAIL_PATH(doctor.doctorId)}
                      className="rounded-lg border border-blue-200 px-4 py-2 font-semibold text-blue-700 hover:bg-blue-50"
                    >
                      상세 보기
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
