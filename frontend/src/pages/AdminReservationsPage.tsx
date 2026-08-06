import { useState } from "react";
import { QueryError } from "../components/QueryError";
import { useAdminReservationsQuery } from "../features/reservations/reservationQueries";
import type { ReservationStatus } from "../types/reservation";

const STATUS_LABEL: Record<ReservationStatus, string> = {
  PENDING: "승인 대기",
  APPROVED: "예약 승인",
  REJECTED: "예약 거절",
  COMPLETED: "진료 완료",
  CANCELLED: "환자 취소",
};
const FILTER_STATUS: ReservationStatus[] = [
  "APPROVED",
  "COMPLETED",
  "CANCELLED",
];

export function AdminReservationsPage() {
  const [date, setDate] = useState("");
  const [status, setStatus] = useState<ReservationStatus | undefined>();
  const [hospitalName, setHospitalName] = useState("");
  const [doctorName, setDoctorName] = useState("");
  const [patientName, setPatientName] = useState("");
  const reservationsQuery = useAdminReservationsQuery({
    date: date || undefined,
    status,
    page: 0,
    size: 100,
  });
  const reservations = (reservationsQuery.data?.content ?? []).filter(
    (item) =>
      item.hospitalName
        .toLowerCase()
        .includes(hospitalName.trim().toLowerCase()) &&
      item.doctorName.toLowerCase().includes(doctorName.trim().toLowerCase()) &&
      item.patientName.toLowerCase().includes(patientName.trim().toLowerCase()),
  );

  return (
    <section>
      <h1 className="text-3xl font-bold">전체 예약 관리</h1>
      <div className="mt-8 grid gap-3 rounded-xl border border-slate-200 bg-white p-5 sm:grid-cols-2 lg:grid-cols-5">
        <FilterInput
          label="병원"
          value={hospitalName}
          onChange={setHospitalName}
        />
        <FilterInput label="의사" value={doctorName} onChange={setDoctorName} />
        <FilterInput
          label="환자"
          value={patientName}
          onChange={setPatientName}
        />
        <label className="text-sm font-medium text-slate-700">
          예약일
          <input
            type="date"
            value={date}
            onChange={(event) => setDate(event.target.value)}
            className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2"
          />
        </label>
        <label className="text-sm font-medium text-slate-700">
          상태
          <select
            value={status ?? ""}
            onChange={(event) =>
              setStatus(
                (event.target.value || undefined) as
                  | ReservationStatus
                  | undefined,
              )
            }
            className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2"
          >
            <option value="">전체</option>
            {FILTER_STATUS.map((value) => (
              <option key={value} value={value}>
                {STATUS_LABEL[value]}
              </option>
            ))}
          </select>
        </label>
      </div>
      <div className="mt-6">
        {reservationsQuery.isPending && (
          <p role="status" className="text-sm text-slate-600">
            예약 목록을 불러오고 있습니다.
          </p>
        )}
        {reservationsQuery.isError && (
          <QueryError
            error={reservationsQuery.error}
            onRetry={() => reservationsQuery.refetch()}
          />
        )}
        {reservationsQuery.isSuccess && reservations.length === 0 && (
          <p className="rounded-lg border border-slate-200 bg-white p-8 text-center text-slate-600">
            조건에 맞는 예약이 없습니다.
          </p>
        )}
        {reservations.length > 0 && (
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
            <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
              <thead className="bg-slate-50 text-slate-600">
                <tr>
                  <th className="px-4 py-3">예약</th>
                  <th className="px-4 py-3">병원</th>
                  <th className="px-4 py-3">의사</th>
                  <th className="px-4 py-3">환자</th>
                  <th className="px-4 py-3">진료 일시</th>
                  <th className="px-4 py-3">상태</th>
                  <th className="px-4 py-3">신청일</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {reservations.map((reservation) => (
                  <tr
                    key={reservation.reservationId}
                    className="hover:bg-slate-50"
                  >
                    <td className="px-4 py-4 text-slate-500">
                      #{reservation.reservationId}
                    </td>
                    <td className="px-4 py-4 font-semibold">
                      {reservation.hospitalName}
                    </td>
                    <td className="px-4 py-4">{reservation.doctorName}</td>
                    <td className="px-4 py-4">{reservation.patientName}</td>
                    <td className="whitespace-nowrap px-4 py-4">
                      {reservation.reservationDate}
                      <p className="mt-1 text-slate-500">
                        {reservation.startTime.slice(0, 5)} ~{" "}
                        {reservation.endTime.slice(0, 5)}
                      </p>
                    </td>
                    <td className="px-4 py-4">
                      <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold">
                        {STATUS_LABEL[reservation.reservationStatus]}
                      </span>
                    </td>
                    <td className="whitespace-nowrap px-4 py-4 text-slate-500">
                      {formatDateTime(reservation.createdAt)}
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

function FilterInput({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <label className="text-sm font-medium text-slate-700">
      {label}
      <input
        type="search"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={`${label} 이름`}
        className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2"
      />
    </label>
  );
}
function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(value));
}
