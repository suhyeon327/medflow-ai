import { useState } from 'react';
import { getApiErrorMessage } from '../api/apiError';
import { QueryError } from '../components/QueryError';
import { useDoctorReservationPatientQuery, useDoctorReservationsQuery, useUpdateDoctorReservationStatusMutation } from '../features/reservations/reservationQueries';
import type { DoctorReservationFilters, DoctorReservationStatusUpdate, ReservationStatus } from '../types/reservation';

const PAGE_SIZE = 10;
const STATUS_LABEL: Record<ReservationStatus, string> = { PENDING: '승인 대기', APPROVED: '진료 예정', REJECTED: '예약 거절', COMPLETED: '진료 완료', CANCELLED: '환자 취소' };
type ReservationTimeRange = 'UPCOMING' | 'PAST' | 'ALL';

export function DoctorReservationsPage() {
  const [filters, setFilters] = useState<DoctorReservationFilters>({ page: 0, size: PAGE_SIZE });
  const [timeRange, setTimeRange] = useState<ReservationTimeRange>('UPCOMING');
  const [selectedReservationId, setSelectedReservationId] = useState<number | null>(null);
  const reservationsQuery = useDoctorReservationsQuery(filters);
  const pendingReservationsQuery = useDoctorReservationsQuery({ status: 'PENDING', page: 0, size: 5 });
  const patientQuery = useDoctorReservationPatientQuery(selectedReservationId);
  const statusMutation = useUpdateDoctorReservationStatusMutation();

  const updateFilter = (key: 'date' | 'status', value: string) => setFilters((current) => ({ ...current, [key]: value || undefined, page: 0 }));
  const updateStatus = (reservationId: number, status: DoctorReservationStatusUpdate) => {
    const label = status === 'APPROVED' ? '승인' : status === 'REJECTED' ? '거절' : '진료 완료 처리';
    if (window.confirm(`이 예약을 ${label}하시겠습니까?`)) statusMutation.mutate({ reservationId, status });
  };
  const pendingReservations = pendingReservationsQuery.data?.content.filter((reservation) => !hasReservationEnded(reservation.reservationDate, reservation.endTime)) ?? [];
  const visibleReservations = reservationsQuery.data?.content.filter((reservation) => {
    if (timeRange === 'ALL') return true;
    const hasEnded = hasReservationEnded(reservation.reservationDate, reservation.endTime);
    return timeRange === 'PAST' ? hasEnded : !hasEnded;
  }) ?? [];

  return (
    <section>
      <div><p className="text-sm font-semibold text-blue-700">진료 관리</p><h1 className="mt-2 text-3xl font-bold">예약 관리</h1><p className="mt-3 text-slate-600">내 진료 예약을 확인하고 환자 정보와 진행 상태를 관리합니다.</p></div>
      <section className="mt-8 rounded-xl border border-amber-200 bg-amber-50 p-5">
        <div className="flex flex-wrap items-center justify-between gap-2"><div><h2 className="font-bold text-amber-950">승인 대기 예약</h2><p className="mt-1 text-sm text-amber-800">승인 또는 거절 처리가 필요한 예약입니다.</p></div>{pendingReservations.length > 0 && <span className="rounded-full bg-amber-200 px-3 py-1 text-sm font-bold text-amber-900">{pendingReservations.length}건</span>}</div>
        {pendingReservationsQuery.isPending && <p className="mt-4 text-sm text-amber-800">승인 대기 예약을 불러오고 있습니다.</p>}
        {pendingReservationsQuery.isError && <p className="mt-4 text-sm text-red-700">승인 대기 예약을 불러오지 못했습니다.</p>}
        {pendingReservationsQuery.isSuccess && pendingReservations.length === 0 && <p className="mt-4 text-sm text-amber-800">현재 처리할 예약이 없습니다.</p>}
        <ul className="mt-4 space-y-3">{pendingReservations.map((reservation) => <li key={reservation.reservationId} className="flex flex-col justify-between gap-3 rounded-lg border border-amber-200 bg-white p-4 sm:flex-row sm:items-center"><div><strong>{reservation.patientName} 환자</strong><p className="mt-1 text-sm text-slate-600">{formatDate(reservation.reservationDate)} · {formatTime(reservation.startTime)} ~ {formatTime(reservation.endTime)}</p></div><div className="flex gap-2"><ActionButton label="승인" onClick={() => updateStatus(reservation.reservationId, 'APPROVED')} disabled={statusMutation.isPending} /><ActionButton label="거절" tone="red" onClick={() => updateStatus(reservation.reservationId, 'REJECTED')} disabled={statusMutation.isPending} /></div></li>)}</ul>
      </section>
      <div className="mt-8 grid gap-3 rounded-xl border border-slate-200 bg-white p-5 sm:grid-cols-3">
        <label className="text-sm font-medium text-slate-700">조회 범위<select value={timeRange} onChange={(event) => setTimeRange(event.target.value as ReservationTimeRange)} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2"><option value="UPCOMING">예정 예약</option><option value="PAST">지난 예약</option><option value="ALL">전체 예약</option></select></label>
        <label className="text-sm font-medium text-slate-700">예약일<input type="date" value={filters.date ?? ''} onChange={(event) => updateFilter('date', event.target.value)} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2" /></label>
        <label className="text-sm font-medium text-slate-700">상태<select value={filters.status ?? ''} onChange={(event) => updateFilter('status', event.target.value)} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2"><option value="">전체</option>{Object.entries(STATUS_LABEL).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
      </div>
      <div className="mt-6">
        {reservationsQuery.isPending && <p role="status" className="text-sm text-slate-600">예약 목록을 불러오고 있습니다.</p>}
        {reservationsQuery.isError && <QueryError error={reservationsQuery.error} onRetry={() => reservationsQuery.refetch()} />}
        {statusMutation.isError && <p role="alert" className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{getApiErrorMessage(statusMutation.error)}</p>}
        {reservationsQuery.isSuccess && visibleReservations.length === 0 && <p className="rounded-lg border border-slate-200 bg-white p-8 text-center text-slate-600">조건에 맞는 예약이 없습니다.</p>}
        <ul className="space-y-4">{visibleReservations.map((reservation) => {
          const hasEnded = hasReservationEnded(reservation.reservationDate, reservation.endTime);
          return <li key={reservation.reservationId} className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"><div className="flex flex-col justify-between gap-4 sm:flex-row"><div><div className="flex flex-wrap items-center gap-2"><h2 className="font-bold">{reservation.patientName} 환자</h2><span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold">{STATUS_LABEL[reservation.reservationStatus]}</span></div><p className="mt-2 text-sm text-slate-600">{formatDate(reservation.reservationDate)} · {formatTime(reservation.startTime)} ~ {formatTime(reservation.endTime)}</p><button type="button" onClick={() => setSelectedReservationId(reservation.reservationId)} className="mt-3 text-sm font-semibold text-blue-700 hover:underline">환자 정보 보기</button></div><div className="flex flex-wrap items-start gap-2">{reservation.reservationStatus === 'PENDING' && <><ActionButton label="승인" onClick={() => updateStatus(reservation.reservationId, 'APPROVED')} disabled={statusMutation.isPending} /><ActionButton label="거절" tone="red" onClick={() => updateStatus(reservation.reservationId, 'REJECTED')} disabled={statusMutation.isPending} /></>}{reservation.reservationStatus === 'APPROVED' && hasEnded && <ActionButton label="진료 완료 처리" onClick={() => updateStatus(reservation.reservationId, 'COMPLETED')} disabled={statusMutation.isPending} />}</div></div></li>;
        })}</ul>
      </div>
      {reservationsQuery.data && reservationsQuery.data.totalPages > 1 && <Pagination page={filters.page} totalPages={reservationsQuery.data.totalPages} onPage={(page) => setFilters((current) => ({ ...current, page }))} />}
      {selectedReservationId !== null && <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4" role="dialog" aria-modal="true" aria-labelledby="patient-dialog-title"><div className="w-full max-w-lg rounded-xl bg-white p-6 shadow-xl"><div className="flex items-center justify-between"><h2 id="patient-dialog-title" className="text-xl font-bold">예약 환자 정보</h2><button type="button" onClick={() => setSelectedReservationId(null)} className="rounded-md px-3 py-2 text-slate-500 hover:bg-slate-100">닫기</button></div>{patientQuery.isPending && <p className="mt-5 text-sm text-slate-600">환자 정보를 불러오고 있습니다.</p>}{patientQuery.isError && <div className="mt-5"><QueryError error={patientQuery.error} onRetry={() => patientQuery.refetch()} /></div>}{patientQuery.data && <dl className="mt-5 grid gap-4 sm:grid-cols-2"><Info label="이름" value={patientQuery.data.patientName} /><Info label="성별" value={patientQuery.data.gender === 'MALE' ? '남성' : '여성'} /><Info label="생년월일" value={patientQuery.data.birthDate} /><Info label="전화번호" value={patientQuery.data.phoneNumber} /></dl>}</div></div>}
    </section>
  );
}

function ActionButton({ label, onClick, disabled, tone = 'blue' }: { label: string; onClick: () => void; disabled: boolean; tone?: 'blue' | 'red' }) { return <button type="button" disabled={disabled} onClick={onClick} className={`rounded-md border px-3 py-2 text-sm font-semibold disabled:opacity-50 ${tone === 'red' ? 'border-red-300 text-red-700 hover:bg-red-50' : 'border-blue-300 text-blue-700 hover:bg-blue-50'}`}>{label}</button>; }
function Info({ label, value }: { label: string; value: string }) { return <div><dt className="text-sm font-medium text-slate-500">{label}</dt><dd className="mt-1 font-medium text-slate-900">{value}</dd></div>; }
function Pagination({ page, totalPages, onPage }: { page: number; totalPages: number; onPage: (page: number) => void }) { return <nav className="mt-8 flex items-center justify-center gap-3"><button type="button" disabled={page === 0} onClick={() => onPage(page - 1)} className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40">이전</button><span className="text-sm text-slate-600">{page + 1} / {totalPages}</span><button type="button" disabled={page + 1 >= totalPages} onClick={() => onPage(page + 1)} className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40">다음</button></nav>; }
function formatDate(value: string) { return new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' }).format(new Date(`${value}T00:00:00`)); }
function formatTime(value: string) { return value.slice(0, 5); }
function hasReservationEnded(date: string, endTime: string) { return new Date(`${date}T${endTime}`).getTime() <= Date.now(); }
