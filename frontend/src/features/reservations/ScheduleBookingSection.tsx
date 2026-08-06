import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../api/apiError";
import { useAuth } from "../../auth/AuthContext";
import { LOGIN_PATH, PATIENT_RESERVATIONS_PATH } from "../../routes/routePaths";
import {
  useAvailableSchedulesQuery,
  useCreateReservationMutation,
} from "./reservationQueries";

function formatDate(date: string): string {
  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "short",
  }).format(new Date(`${date}T00:00:00`));
}

export function ScheduleBookingSection({ doctorId }: { doctorId: number }) {
  const { user } = useAuth();
  const canReserve = user?.role === "PATIENT";
  const schedulesQuery = useAvailableSchedulesQuery(doctorId);
  const createMutation = useCreateReservationMutation(doctorId);
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedScheduleId, setSelectedScheduleId] = useState<number | null>(
    null,
  );
  const availableSchedules = useMemo(
    () =>
      (schedulesQuery.data ?? [])
        .filter(
          (schedule) =>
            new Date(`${schedule.date}T${schedule.startTime}`).getTime() >
            Date.now(),
        )
        .sort((left, right) =>
          `${left.date}T${left.startTime}`.localeCompare(
            `${right.date}T${right.startTime}`,
          ),
        ),
    [schedulesQuery.data],
  );
  const dates = useMemo(
    () => [...new Set(availableSchedules.map((schedule) => schedule.date))],
    [availableSchedules],
  );
  const activeDate = dates.includes(selectedDate)
    ? selectedDate
    : (dates[0] ?? "");
  const dateIndex = dates.indexOf(activeDate);
  const daySchedules = availableSchedules.filter(
    (schedule) => schedule.date === activeDate,
  );
  const selectedSchedule = availableSchedules.find(
    (schedule) => schedule.scheduleId === selectedScheduleId,
  );

  const moveDate = (direction: number) => {
    const nextDate = dates[dateIndex + direction];
    if (nextDate) {
      setSelectedDate(nextDate);
      setSelectedScheduleId(null);
    }
  };

  return (
    <section className="mt-8 rounded-2xl border border-slate-200 bg-white p-7 shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <h2 className="text-xl font-extrabold">예약 가능한 시간</h2>
          {activeDate && (
            <>
              <button
                type="button"
                disabled={dateIndex <= 0}
                onClick={() => moveDate(-1)}
                className="rounded-lg px-3 py-2 text-blue-600 disabled:text-slate-300"
              >
                ‹
              </button>
              <div className="rounded-lg border border-slate-200 bg-slate-50 px-5 py-2.5 text-sm font-semibold">
                {formatDate(activeDate)}
              </div>
              <button
                type="button"
                disabled={dateIndex < 0 || dateIndex + 1 >= dates.length}
                onClick={() => moveDate(1)}
                className="rounded-lg px-3 py-2 text-blue-600 disabled:text-slate-300"
              >
                ›
              </button>
            </>
          )}
        </div>
        <p className="text-sm text-slate-500">
          ⓘ 원하는 시간을 선택하여 예약할 수 있습니다.
        </p>
      </div>
      {!user && (
        <p className="mt-4 text-sm text-slate-600">
          예약은{" "}
          <Link to={LOGIN_PATH} className="font-semibold text-blue-700">
            환자 로그인
          </Link>{" "}
          후 가능합니다.
        </p>
      )}
      {user && !canReserve && (
        <p className="mt-4 text-sm text-slate-600">
          예약 요청은 환자 계정만 가능합니다.
        </p>
      )}
      {schedulesQuery.isPending && (
        <p role="status" className="mt-7 text-sm text-slate-600">
          예약 가능 시간을 불러오고 있습니다.
        </p>
      )}
      {schedulesQuery.isError && (
        <p
          role="alert"
          className="mt-7 rounded-md bg-red-50 p-3 text-sm text-red-700"
        >
          {getApiErrorMessage(schedulesQuery.error)}
        </p>
      )}
      {schedulesQuery.isSuccess && availableSchedules.length === 0 && (
        <p className="mt-7 text-sm text-slate-600">
          현재 예약 가능한 시간이 없습니다.
        </p>
      )}
      {daySchedules.length > 0 && (
        <div className="mt-7 grid gap-8 md:grid-cols-2">
          <TimeGroup
            label="오전"
            schedules={daySchedules.filter(
              (schedule) => Number(schedule.startTime.slice(0, 2)) < 12,
            )}
            selectedId={selectedScheduleId}
            onSelect={setSelectedScheduleId}
          />
          <TimeGroup
            label="오후"
            schedules={daySchedules.filter(
              (schedule) => Number(schedule.startTime.slice(0, 2)) >= 12,
            )}
            selectedId={selectedScheduleId}
            onSelect={setSelectedScheduleId}
          />
        </div>
      )}
      {selectedSchedule && (
        <div className="mt-7 flex flex-col items-center justify-between gap-4 rounded-xl bg-blue-50 px-5 py-4 sm:flex-row">
          <p className="text-sm">
            <span className="mr-2 font-bold text-blue-600">◷ 선택한 시간:</span>
            {formatDate(selectedSchedule.date)}{" "}
            {selectedSchedule.startTime.slice(0, 5)}
          </p>
          {canReserve && (
            <button
              type="button"
              disabled={createMutation.isPending}
              onClick={() =>
                createMutation.mutate({
                  scheduleId: selectedSchedule.scheduleId,
                })
              }
              className="min-w-40 rounded-lg bg-blue-600 px-6 py-3 text-sm font-bold text-white hover:bg-blue-700 disabled:opacity-60"
            >
              {createMutation.isPending ? "예약 중..." : "예약하기"}
            </button>
          )}
        </div>
      )}
      {canReserve && createMutation.isError && (
        <p
          role="alert"
          className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700"
        >
          {getApiErrorMessage(createMutation.error)}
        </p>
      )}
      {canReserve && createMutation.isSuccess && (
        <p
          role="status"
          className="mt-4 rounded-md bg-green-50 p-3 text-sm text-green-700"
        >
          예약했습니다.{" "}
          <Link
            to={PATIENT_RESERVATIONS_PATH}
            className="font-semibold underline"
          >
            내 예약 확인
          </Link>
        </p>
      )}
    </section>
  );
}

type Schedule = { scheduleId: number; startTime: string; endTime: string };
function TimeGroup({
  label,
  schedules,
  selectedId,
  onSelect,
}: {
  label: string;
  schedules: Schedule[];
  selectedId: number | null;
  onSelect: (id: number) => void;
}) {
  return (
    <div>
      <h3 className="mb-3 text-sm font-bold text-slate-700">{label}</h3>
      <div className="flex flex-wrap gap-3">
        {schedules.map((schedule) => (
          <button
            key={schedule.scheduleId}
            type="button"
            onClick={() => onSelect(schedule.scheduleId)}
            className={`min-w-20 rounded-lg border px-4 py-2.5 text-sm font-bold ${selectedId === schedule.scheduleId ? "border-blue-600 bg-blue-600 text-white" : "border-blue-200 bg-white text-blue-600 hover:bg-blue-50"}`}
          >
            {schedule.startTime.slice(0, 5)}
          </button>
        ))}
      </div>
    </div>
  );
}
