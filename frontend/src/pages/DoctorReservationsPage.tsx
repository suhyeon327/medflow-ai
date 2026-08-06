import { useState, type FormEvent, type ReactNode } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../api/apiError";
import { QueryError } from "../components/QueryError";
import {
  useCreateDoctorSchedulesMutation,
  useOwnDoctorSchedulesQuery,
} from "../features/doctors/doctorQueries";
import { useDoctorReservationsQuery } from "../features/reservations/reservationQueries";
import { DOCTOR_RESERVATION_DETAIL_PATH } from "../routes/routePaths";
import type { ReservationStatus } from "../types/reservation";

const INPUT_CLASS =
  "mt-2 w-full rounded-lg border border-slate-300 px-3 py-2.5";
const RESERVATION_QUERY_SIZE = 100;
const STATUS_LABEL: Record<ReservationStatus, string> = {
  PENDING: "예약됨",
  APPROVED: "예약됨",
  REJECTED: "예약 거절",
  COMPLETED: "진료 완료",
  CANCELLED: "예약 취소",
};

export function DoctorReservationsPage() {
  const today = getLocalDateString();
  const [date, setDate] = useState(today);
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({
    date: today,
    startTime: "09:00",
    endTime: "18:00",
    slotMinutes: 30,
  });
  const reservationsQuery = useDoctorReservationsQuery({
    date,
    page: 0,
    size: RESERVATION_QUERY_SIZE,
  });
  const schedulesQuery = useOwnDoctorSchedulesQuery(date);
  const createMutation = useCreateDoctorSchedulesMutation();
  const reservations =
    reservationsQuery.data?.content.filter(
      (item) =>
        item.reservationStatus !== "REJECTED" &&
        item.reservationStatus !== "CANCELLED",
    ) ?? [];
  const scheduleRows = (schedulesQuery.data ?? [])
    .map((schedule) => ({
      schedule,
      reservation: reservations.find(
        (item) =>
          item.startTime === schedule.startTime &&
          item.endTime === schedule.endTime,
      ),
    }))
    .sort((a, b) => a.schedule.startTime.localeCompare(b.schedule.startTime));

  const changeDate = (days: number) => {
    const next = new Date(`${date}T00:00:00`);
    next.setDate(next.getDate() + days);
    setDate(getLocalDateString(next));
  };

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (form.startTime >= form.endTime) return;
    createMutation.mutate({
      ...form,
      startTime: `${form.startTime}:00`,
      endTime: `${form.endTime}:00`,
    });
  };

  return (
    <section>
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <h1 className="text-3xl font-bold">진료 관리</h1>
        <button
          type="button"
          onClick={() => setShowCreate((value) => !value)}
          className="rounded-xl bg-blue-600 px-5 py-3 font-semibold text-white"
        >
          ＋ 스케줄 생성
        </button>
      </div>

      {showCreate && (
        <form
          onSubmit={submit}
          className="mt-6 grid gap-4 rounded-2xl border border-blue-200 bg-blue-50 p-5 sm:grid-cols-5"
        >
          <FormField label="진료일">
            <input
              type="date"
              required
              value={form.date}
              onChange={(event) =>
                setForm({ ...form, date: event.target.value })
              }
              className={INPUT_CLASS}
            />
          </FormField>
          <FormField label="시작 시간">
            <input
              type="time"
              required
              value={form.startTime}
              onChange={(event) =>
                setForm({ ...form, startTime: event.target.value })
              }
              className={INPUT_CLASS}
            />
          </FormField>
          <FormField label="종료 시간">
            <input
              type="time"
              required
              value={form.endTime}
              onChange={(event) =>
                setForm({ ...form, endTime: event.target.value })
              }
              className={INPUT_CLASS}
            />
          </FormField>
          <FormField label="진료 간격">
            <select
              value={form.slotMinutes}
              onChange={(event) =>
                setForm({ ...form, slotMinutes: Number(event.target.value) })
              }
              className={INPUT_CLASS}
            >
              {[10, 15, 20, 30, 40, 50, 60].map((minute) => (
                <option key={minute} value={minute}>
                  {minute}분
                </option>
              ))}
            </select>
          </FormField>
          <button
            disabled={
              createMutation.isPending || form.startTime >= form.endTime
            }
            className="self-end rounded-lg bg-blue-600 px-4 py-2.5 font-semibold text-white disabled:opacity-50"
          >
            {createMutation.isPending ? "생성 중..." : "시간 추가"}
          </button>
          {form.startTime >= form.endTime && (
            <p className="text-sm text-red-700 sm:col-span-5">
              종료 시간은 시작 시간보다 늦어야 합니다.
            </p>
          )}
          {createMutation.isError && (
            <p className="text-sm text-red-700 sm:col-span-5">
              {getApiErrorMessage(createMutation.error)}
            </p>
          )}
        </form>
      )}

      <section className="mt-7 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex flex-wrap items-center gap-3">
          <button
            type="button"
            onClick={() => changeDate(-1)}
            className="rounded-lg border border-slate-200 px-4 py-3"
          >
            ‹
          </button>
          <div className="min-w-52 rounded-lg border border-slate-200 px-5 py-3 text-center font-bold">
            📅 {formatDate(date)}
          </div>
          <button
            type="button"
            onClick={() => changeDate(1)}
            className="rounded-lg border border-slate-200 px-4 py-3"
          >
            ›
          </button>
          <button
            type="button"
            onClick={() => setDate(today)}
            className="rounded-lg border border-blue-200 px-4 py-3 font-semibold text-blue-700"
          >
            오늘
          </button>
        </div>
        <div className="mt-6 grid gap-3 sm:grid-cols-3">
          <Stat label="등록 스케줄" value={schedulesQuery.data?.length ?? 0} />
          <Stat
            label="예약됨"
            value={
              reservations.filter(
                (item) => item.reservationStatus !== "COMPLETED",
              ).length
            }
            tone="blue"
          />
          <Stat
            label="예약 완료"
            value={
              reservations.filter(
                (item) => item.reservationStatus === "COMPLETED",
              ).length
            }
            tone="emerald"
          />
        </div>
      </section>

      <div className="mt-6">
        {reservationsQuery.isPending && (
          <p role="status" className="text-sm text-slate-600">
            진료 내역을 불러오고 있습니다.
          </p>
        )}
        {reservationsQuery.isError && (
          <QueryError
            error={reservationsQuery.error}
            onRetry={() => reservationsQuery.refetch()}
          />
        )}
        {schedulesQuery.isError && (
          <QueryError
            error={schedulesQuery.error}
            onRetry={() => schedulesQuery.refetch()}
          />
        )}
        {reservationsQuery.isSuccess &&
          schedulesQuery.isSuccess &&
          scheduleRows.length === 0 && (
            <p className="rounded-xl border border-slate-200 bg-white p-10 text-center text-slate-500">
              선택한 날짜에 등록된 진료 시간표가 없습니다.
            </p>
          )}
        {scheduleRows.length > 0 && (
          <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
            <table className="min-w-full text-center text-sm">
              <thead className="bg-slate-50 text-slate-600">
                <tr>
                  <th className="px-5 py-4">시간</th>
                  <th className="px-5 py-4">상태</th>
                  <th className="px-5 py-4">환자</th>
                  <th className="px-5 py-4">상세</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {scheduleRows.map(({ schedule, reservation }) => (
                  <tr key={schedule.scheduleId} className="hover:bg-slate-50">
                    <td className="whitespace-nowrap px-5 py-4 font-semibold">
                      {schedule.startTime.slice(0, 5)} ~{" "}
                      {schedule.endTime.slice(0, 5)}
                    </td>
                    <td className="px-5 py-4">
                      <span
                        className={`rounded-full px-3 py-1 text-xs font-semibold ${getReservationStatusClass(reservation?.reservationStatus)}`}
                      >
                        {reservation
                          ? STATUS_LABEL[reservation.reservationStatus]
                          : "예약 가능"}
                      </span>
                    </td>
                    <td className="px-5 py-4 font-semibold">
                      {reservation ? `${reservation.patientName} 환자` : "-"}
                    </td>
                    <td className="px-5 py-4">
                      {reservation ? (
                        <Link
                          to={DOCTOR_RESERVATION_DETAIL_PATH(
                            reservation.reservationId,
                            reservation.questionnaireId,
                          )}
                          className="rounded-lg border border-blue-200 px-3 py-2 font-semibold text-blue-700 hover:bg-blue-50"
                        >
                          상세 보기
                        </Link>
                      ) : (
                        "-"
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}

function FormField({
  label,
  children,
}: {
  label: string;
  children: ReactNode;
}) {
  return (
    <label className="text-sm font-medium">
      {label}
      {children}
    </label>
  );
}
function Stat({
  label,
  value,
  tone = "slate",
}: {
  label: string;
  value: number;
  tone?: "slate" | "blue" | "emerald";
}) {
  const color = {
    slate: "text-slate-800",
    blue: "text-blue-700",
    emerald: "text-emerald-700",
  }[tone];
  return (
    <div className="rounded-xl border border-slate-200 p-4">
      <p className="text-sm text-slate-500">{label}</p>
      <p className={`mt-2 text-2xl font-bold ${color}`}>{value}건</p>
    </div>
  );
}
function getLocalDateString(date = new Date()) {
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

function getReservationStatusClass(status?: ReservationStatus): string {
  if (!status) return "bg-emerald-50 text-emerald-700";
  if (status === "COMPLETED") return "bg-slate-100 text-slate-700";
  return "bg-blue-50 text-blue-700";
}
