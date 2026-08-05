import { useState } from 'react';
import { Link } from 'react-router-dom';
import { getApiErrorMessage } from '../api/apiError';
import { QueryError } from '../components/QueryError';
import { useCancelReservationMutation, usePatientReservationsQuery } from '../features/reservations/reservationQueries';
import { DOCTOR_DETAIL_PATH, HOSPITALS_PATH } from '../routes/routePaths';
import type { ReservationFilters, ReservationPeriod, ReservationStatus } from '../types/reservation';

const STATUS_LABEL: Record<ReservationStatus, string> = {
  REQUESTED: '승인 대기', CONFIRMED: '예약 확정', COMPLETED: '진료 완료', CANCELLED: '취소',
};
const PERIOD_LABEL: Record<ReservationPeriod, string> = {
  UPCOMING: '예정', TODAY: '오늘', PAST: '지난 예약',
};
const PAGE_SIZE = 10;

function formatDate(date: string): string {
  return new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' }).format(new Date(`${date}T00:00:00`));
}

function isFutureReservation(date: string, startTime: string): boolean {
  return new Date(`${date}T${startTime}`).getTime() > Date.now();
}

export function PatientReservationsPage() {
  const [filters, setFilters] = useState<ReservationFilters>({ page: 0, size: PAGE_SIZE });
  const reservationsQuery = usePatientReservationsQuery(filters);
  const cancelMutation = useCancelReservationMutation();

  const updateFilter = <K extends keyof ReservationFilters>(key: K, value: ReservationFilters[K]) => {
    setFilters((current) => ({ ...current, [key]: value || undefined, page: 0 }));
  };

  const handleCancel = (reservationId: number, doctorId: number) => {
    if (window.confirm('이 예약을 취소하시겠습니까?')) {
      cancelMutation.mutate({ reservationId, doctorId });
    }
  };

  return (
    <section>
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="text-sm font-semibold text-blue-700">예약 관리</p>
          <h1 className="mt-2 text-3xl font-bold">내 예약</h1>
        </div>
        <Link to={HOSPITALS_PATH} className="rounded-md bg-blue-600 px-4 py-2.5 text-center text-sm font-semibold text-white hover:bg-blue-700">새 예약 찾기</Link>
      </div>

      <div className="mt-8 grid gap-3 rounded-xl border border-slate-200 bg-white p-5 sm:grid-cols-3">
        <label className="text-sm font-medium text-slate-700">기간
          <select value={filters.period ?? ''} onChange={(event) => updateFilter('period', (event.target.value || undefined) as ReservationPeriod | undefined)} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2">
            <option value="">전체</option>
            {Object.entries(PERIOD_LABEL).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </label>
        <label className="text-sm font-medium text-slate-700">상태
          <select value={filters.status ?? ''} onChange={(event) => updateFilter('status', (event.target.value || undefined) as ReservationStatus | undefined)} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2">
            <option value="">전체</option>
            {Object.entries(STATUS_LABEL).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </label>
        <label className="text-sm font-medium text-slate-700">예약일
          <input type="date" value={filters.date ?? ''} onChange={(event) => updateFilter('date', event.target.value)} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2" />
        </label>
      </div>

      <div className="mt-6">
        {reservationsQuery.isPending && <p role="status" className="text-sm text-slate-600">예약 내역을 불러오고 있습니다.</p>}
        {reservationsQuery.isError && <QueryError error={reservationsQuery.error} onRetry={() => reservationsQuery.refetch()} />}
        {cancelMutation.isError && <p role="alert" className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{getApiErrorMessage(cancelMutation.error)}</p>}
        {reservationsQuery.data?.content.length === 0 && <p className="rounded-lg border border-slate-200 bg-white p-8 text-center text-slate-600">조건에 맞는 예약이 없습니다.</p>}
        {reservationsQuery.data && reservationsQuery.data.content.length > 0 && (
          <ul className="space-y-4">
            {reservationsQuery.data.content.map((reservation) => {
              const cancellable = (reservation.reservationStatus === 'REQUESTED' || reservation.reservationStatus === 'CONFIRMED')
                && isFutureReservation(reservation.reservationDate, reservation.startTime);
              const cancelling = cancelMutation.isPending && cancelMutation.variables?.reservationId === reservation.reservationId;
              return (
                <li key={reservation.reservationId} className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                  <div className="flex flex-col justify-between gap-4 sm:flex-row">
                    <div>
                      <div className="flex items-center gap-2"><h2 className="font-bold">{reservation.hospitalName}</h2><span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700">{STATUS_LABEL[reservation.reservationStatus]}</span></div>
                      <p className="mt-2 text-sm text-slate-700">{reservation.doctorName} 의사</p>
                      <p className="mt-1 text-sm text-slate-600">{formatDate(reservation.reservationDate)} · {reservation.startTime.slice(0, 5)} ~ {reservation.endTime.slice(0, 5)}</p>
                      <Link to={DOCTOR_DETAIL_PATH(reservation.doctorId)} className="mt-3 inline-block text-sm font-semibold text-blue-700">의사 정보 보기</Link>
                    </div>
                    {cancellable && (
                      <button type="button" disabled={cancelMutation.isPending} onClick={() => handleCancel(reservation.reservationId, reservation.doctorId)} className="self-start rounded-md border border-red-300 px-3 py-2 text-sm font-semibold text-red-700 hover:bg-red-50 disabled:opacity-60">
                        {cancelling ? '취소 중...' : '예약 취소'}
                      </button>
                    )}
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      {reservationsQuery.data && reservationsQuery.data.totalPages > 1 && (
        <nav className="mt-8 flex items-center justify-center gap-3" aria-label="예약 페이지">
          <button type="button" disabled={filters.page === 0} onClick={() => setFilters((current) => ({ ...current, page: current.page - 1 }))} className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40">이전</button>
          <span className="text-sm text-slate-600">{filters.page + 1} / {reservationsQuery.data.totalPages}</span>
          <button type="button" disabled={filters.page + 1 >= reservationsQuery.data.totalPages} onClick={() => setFilters((current) => ({ ...current, page: current.page + 1 }))} className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40">다음</button>
        </nav>
      )}
    </section>
  );
}