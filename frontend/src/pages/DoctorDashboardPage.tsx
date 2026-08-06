import { Link } from "react-router-dom";
import { QueryError } from "../components/QueryError";
import { useDoctorProfileQuery } from "../features/doctors/doctorQueries";
import { useDoctorReservationsQuery } from "../features/reservations/reservationQueries";
import {
  DOCTOR_RESERVATION_DETAIL_PATH,
  DOCTOR_RESERVATIONS_PATH,
} from "../routes/routePaths";

export function DoctorDashboardPage() {
  const today = getLocalDateString();
  const profileQuery = useDoctorProfileQuery();
  const reservationsQuery = useDoctorReservationsQuery({
    date: today,
    page: 0,
    size: 100,
  });
  const reservations =
    reservationsQuery.data?.content.filter(
      (item) =>
        item.reservationStatus !== "REJECTED" &&
        item.reservationStatus !== "CANCELLED",
    ) ?? [];
  const nextReservation = reservations
    .filter(
      (reservation) =>
        reservation.reservationStatus !== "COMPLETED" &&
        new Date(
          `${reservation.reservationDate}T${reservation.endTime}`,
        ).getTime() > Date.now(),
    )
    .sort((a, b) => a.startTime.localeCompare(b.startTime))[0];

  return (
    <section>
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <h1 className="text-3xl font-bold">
          {profileQuery.data?.doctorName ?? "의사"}
        </h1>
        <div className="rounded-xl border border-slate-200 bg-white px-4 py-3 font-semibold">
          📅 {formatDate(today)}
        </div>
      </div>
      {reservationsQuery.isError && (
        <div className="mt-6">
          <QueryError
            error={reservationsQuery.error}
            onRetry={() => reservationsQuery.refetch()}
          />
        </div>
      )}
      <div className="mt-7 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <DashboardStat
          label="오늘 진료"
          value={`${reservations.length}건`}
          color="blue"
        />
        <DashboardStat
          label="예약됨"
          value={`${reservations.filter((item) => item.reservationStatus !== "COMPLETED").length}건`}
          color="violet"
        />
        <DashboardStat
          label="예약 완료"
          value={`${reservations.filter((item) => item.reservationStatus === "COMPLETED").length}건`}
          color="emerald"
        />
        <DashboardStat
          label="다음 진료"
          value={nextReservation ? nextReservation.startTime.slice(0, 5) : "-"}
          detail={
            nextReservation ? `${nextReservation.patientName} 환자` : undefined
          }
          color="slate"
        />
      </div>
      <section className="mt-7 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold">오늘의 진료 관리</h2>
          <Link
            to={DOCTOR_RESERVATIONS_PATH}
            className="text-sm font-semibold text-blue-700"
          >
            전체 보기 ›
          </Link>
        </div>
        <div className="mt-5 overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600">
              <tr>
                <th className="px-4 py-3">시간</th>
                <th className="px-4 py-3">상태</th>
                <th className="px-4 py-3">환자</th>
                <th className="px-4 py-3 text-right">상세</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {reservations.slice(0, 6).map((reservation) => (
                <tr key={reservation.reservationId}>
                  <td className="px-4 py-4 font-semibold">
                    {reservation.startTime.slice(0, 5)} ~{" "}
                    {reservation.endTime.slice(0, 5)}
                  </td>
                  <td className="px-4 py-4">
                    {reservation.reservationStatus === "COMPLETED"
                      ? "예약 완료"
                      : "예약됨"}
                  </td>
                  <td className="px-4 py-4 font-semibold">
                    {reservation.patientName} 환자
                  </td>
                  <td className="px-4 py-4 text-right">
                    <Link
                      to={DOCTOR_RESERVATION_DETAIL_PATH(
                        reservation.reservationId,
                        reservation.questionnaireId,
                      )}
                      className="font-semibold text-blue-700"
                    >
                      상세 보기
                    </Link>
                  </td>
                </tr>
              ))}
              {reservations.length === 0 && (
                <tr>
                  <td colSpan={4} className="py-10 text-center text-slate-500">
                    오늘 진료 예약이 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}

function DashboardStat({
  label,
  value,
  detail,
  color,
}: {
  label: string;
  value: string;
  detail?: string;
  color: "blue" | "emerald" | "violet" | "slate";
}) {
  const style = {
    blue: "bg-blue-50 text-blue-700",
    emerald: "bg-emerald-50 text-emerald-700",
    violet: "bg-violet-50 text-violet-700",
    slate: "bg-slate-100 text-slate-700",
  }[color];
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <span className={`rounded-lg px-3 py-2 text-sm font-semibold ${style}`}>
        {label}
      </span>
      <p className="mt-5 text-3xl font-bold">{value}</p>
      {detail && <p className="mt-2 text-sm text-slate-500">{detail}</p>}
    </div>
  );
}
function getLocalDateString() {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}
function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "short",
  }).format(new Date(`${value}T00:00:00`));
}
