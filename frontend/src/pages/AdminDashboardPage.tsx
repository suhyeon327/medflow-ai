import { Link } from "react-router-dom";
import { QueryError } from "../components/QueryError";
import { useAdminHospitalsQuery } from "../features/admin/hospitals/adminHospitalQueries";
import { useAdminUsersQuery } from "../features/admin/users/adminUserQueries";
import { useAdminReservationsQuery } from "../features/reservations/reservationQueries";
import {
  ADMIN_RESERVATIONS_PATH,
  ADMIN_USERS_PATH,
} from "../routes/routePaths";
import type { ReservationStatus } from "../types/reservation";

const STATUS_LABEL: Record<ReservationStatus, string> = {
  PENDING: "승인 대기",
  APPROVED: "예약 완료",
  REJECTED: "예약 거절",
  COMPLETED: "진료 완료",
  CANCELLED: "예약 취소",
};

export function AdminDashboardPage() {
  const hospitalsQuery = useAdminHospitalsQuery();
  const usersQuery = useAdminUsersQuery({ page: 0, size: 5 });
  const reservationsQuery = useAdminReservationsQuery({ page: 0, size: 100 });
  const completedQuery = useAdminReservationsQuery({
    status: "COMPLETED",
    page: 0,
    size: 1,
  });
  const reservations = reservationsQuery.data?.content ?? [];
  const count = (status: ReservationStatus) =>
    reservations.filter((item) => item.reservationStatus === status).length;
  const chartTotal = Math.max(reservations.length, 1);

  return (
    <section>
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-extrabold">관리자</h1>
        <p className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600">
          {formatNow()}
        </p>
      </div>
      {(hospitalsQuery.isError ||
        usersQuery.isError ||
        reservationsQuery.isError) && (
        <div className="mt-6">
          <QueryError
            error={
              hospitalsQuery.error ||
              usersQuery.error ||
              reservationsQuery.error
            }
          />
        </div>
      )}
      <div className="mt-7 grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        <Stat
          label="총 병원 수"
          value={`${hospitalsQuery.data?.length ?? 0}개`}
        />
        <Stat
          label="총 예약 수"
          value={`${reservationsQuery.data?.totalElements ?? 0}건`}
        />
        <Stat
          label="총 사용자 수"
          value={`${usersQuery.data?.totalElements ?? 0}명`}
        />
        <Stat
          label="진료 완료"
          value={`${completedQuery.data?.totalElements ?? 0}건`}
        />
      </div>
      <div className="mt-6 grid gap-6 xl:grid-cols-[1.35fr_1fr]">
        <Panel title="최근 예약" link={ADMIN_RESERVATIONS_PATH}>
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500">
                <tr>
                  <th className="px-4 py-3">예약일시</th>
                  <th className="px-4 py-3">환자명</th>
                  <th className="px-4 py-3">병원명</th>
                  <th className="px-4 py-3">의사명</th>
                  <th className="px-4 py-3">상태</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {reservations.slice(0, 5).map((item) => (
                  <tr key={item.reservationId}>
                    <td className="px-4 py-3">
                      {item.reservationDate} {item.startTime.slice(0, 5)}
                    </td>
                    <td className="px-4 py-3 font-semibold">
                      {item.patientName}
                    </td>
                    <td className="px-4 py-3">{item.hospitalName}</td>
                    <td className="px-4 py-3">{item.doctorName}</td>
                    <td className="px-4 py-3">
                      {STATUS_LABEL[item.reservationStatus]}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Panel>
        <Panel title="최근 가입 사용자" link={ADMIN_USERS_PATH}>
          <ul className="divide-y divide-slate-100">
            {usersQuery.data?.content.map((user) => (
              <li
                key={user.userId}
                className="flex items-center justify-between py-3"
              >
                <div>
                  <p className="font-semibold">{user.email}</p>
                  <p className="text-xs text-slate-500">
                    {roleLabel(user.role)}
                  </p>
                </div>
                <span className="text-sm text-slate-500">
                  {formatDate(user.createdAt)}
                </span>
              </li>
            ))}
          </ul>
        </Panel>
      </div>
      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-lg font-bold">예약 현황</h2>
          <div className="mt-7 flex h-48 items-end gap-5">
            {(
              ["APPROVED", "COMPLETED", "CANCELLED"] as ReservationStatus[]
            ).map((status) => (
              <div
                key={status}
                className="flex flex-1 flex-col items-center gap-2"
              >
                <strong>{count(status)}</strong>
                <div
                  className="w-full rounded-t-lg bg-blue-500"
                  style={{
                    height: `${Math.max((count(status) / chartTotal) * 160, 8)}px`,
                  }}
                />
                <span className="text-xs text-slate-500">
                  {STATUS_LABEL[status]}
                </span>
              </div>
            ))}
          </div>
        </section>
        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-lg font-bold">예약 상태 비율</h2>
          <div className="mt-7 space-y-5">
            {(
              ["APPROVED", "COMPLETED", "CANCELLED"] as ReservationStatus[]
            ).map((status) => {
              const percent = Math.round((count(status) / chartTotal) * 100);
              return (
                <div key={status}>
                  <div className="flex justify-between text-sm">
                    <span>{STATUS_LABEL[status]}</span>
                    <strong>{percent}%</strong>
                  </div>
                  <div className="mt-2 h-2 rounded-full bg-slate-200">
                    <div
                      className="h-full rounded-full bg-blue-500"
                      style={{ width: `${percent}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      </div>
    </section>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-3 text-3xl font-extrabold">{value}</p>
    </div>
  );
}
function Panel({
  title,
  link,
  children,
}: {
  title: string;
  link: string;
  children: React.ReactNode;
}) {
  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="mb-4 flex justify-between">
        <h2 className="text-lg font-bold">{title}</h2>
        <Link to={link} className="text-sm font-bold text-blue-600">
          더보기 ›
        </Link>
      </div>
      {children}
    </section>
  );
}
function roleLabel(role: string) {
  return { PATIENT: "환자", DOCTOR: "의사", ADMIN: "관리자" }[role] ?? role;
}
function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium" }).format(
    new Date(value),
  );
}
function formatNow() {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "long",
    timeStyle: "short",
  }).format(new Date());
}
