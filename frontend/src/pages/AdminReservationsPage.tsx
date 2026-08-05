import { useState } from 'react';
import { QueryError } from '../components/QueryError';
import { useAdminReservationsQuery } from '../features/reservations/reservationQueries';
import type { AdminReservationFilters, ReservationStatus } from '../types/reservation';

const PAGE_SIZE = 20;
const STATUS_LABEL: Record<ReservationStatus, string> = { PENDING: '승인 대기', APPROVED: '예약 승인', REJECTED: '예약 거절', COMPLETED: '진료 완료', CANCELLED: '환자 취소' };

export function AdminReservationsPage() {
  const [filters, setFilters] = useState<AdminReservationFilters>({ page: 0, size: PAGE_SIZE });
  const reservationsQuery = useAdminReservationsQuery(filters);
  const updateFilter = (key: keyof Omit<AdminReservationFilters, 'page' | 'size'>, value: string) => {
    const numeric = key === 'hospitalId' || key === 'doctorId' || key === 'patientId';
    setFilters((current) => ({ ...current, [key]: value ? (numeric ? Number(value) : value) : undefined, page: 0 }));
  };

  return (
    <section>
      <div><p className="text-sm font-semibold text-blue-700">관리자</p><h1 className="mt-2 text-3xl font-bold">전체 예약 관리</h1><p className="mt-3 text-slate-600">병원·의사·환자와 예약 상태를 기준으로 전체 예약을 조회합니다.</p></div>
      <div className="mt-8 grid gap-3 rounded-xl border border-slate-200 bg-white p-5 sm:grid-cols-2 lg:grid-cols-5">
        <FilterInput label="병원 ID" type="number" value={filters.hospitalId?.toString() ?? ''} onChange={(value) => updateFilter('hospitalId', value)} />
        <FilterInput label="의사 ID" type="number" value={filters.doctorId?.toString() ?? ''} onChange={(value) => updateFilter('doctorId', value)} />
        <FilterInput label="환자 ID" type="number" value={filters.patientId?.toString() ?? ''} onChange={(value) => updateFilter('patientId', value)} />
        <FilterInput label="예약일" type="date" value={filters.date ?? ''} onChange={(value) => updateFilter('date', value)} />
        <label className="text-sm font-medium text-slate-700">상태<select value={filters.status ?? ''} onChange={(event) => updateFilter('status', event.target.value)} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2"><option value="">전체</option>{Object.entries(STATUS_LABEL).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
      </div>
      <div className="mt-6">
        {reservationsQuery.isPending && <p role="status" className="text-sm text-slate-600">예약 목록을 불러오고 있습니다.</p>}
        {reservationsQuery.isError && <QueryError error={reservationsQuery.error} onRetry={() => reservationsQuery.refetch()} />}
        {reservationsQuery.data?.content.length === 0 && <p className="rounded-lg border border-slate-200 bg-white p-8 text-center text-slate-600">조건에 맞는 예약이 없습니다.</p>}
        {reservationsQuery.data && reservationsQuery.data.content.length > 0 && <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm"><table className="min-w-full divide-y divide-slate-200 text-left text-sm"><thead className="bg-slate-50 text-slate-600"><tr><th className="px-4 py-3">예약</th><th className="px-4 py-3">병원</th><th className="px-4 py-3">의사</th><th className="px-4 py-3">환자</th><th className="px-4 py-3">진료 일시</th><th className="px-4 py-3">상태</th><th className="px-4 py-3">신청일</th></tr></thead><tbody className="divide-y divide-slate-100">{reservationsQuery.data.content.map((reservation) => <tr key={reservation.reservationId} className="hover:bg-slate-50"><td className="whitespace-nowrap px-4 py-4 text-slate-500">#{reservation.reservationId}</td><td className="px-4 py-4"><strong>{reservation.hospitalName}</strong><p className="mt-1 text-xs text-slate-500">ID {reservation.hospitalId}</p></td><td className="whitespace-nowrap px-4 py-4">{reservation.doctorName}<p className="mt-1 text-xs text-slate-500">ID {reservation.doctorId}</p></td><td className="whitespace-nowrap px-4 py-4">{reservation.patientName}<p className="mt-1 text-xs text-slate-500">ID {reservation.patientId}</p></td><td className="whitespace-nowrap px-4 py-4">{reservation.reservationDate}<p className="mt-1 text-slate-500">{reservation.startTime.slice(0, 5)} ~ {reservation.endTime.slice(0, 5)}</p></td><td className="whitespace-nowrap px-4 py-4"><span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold">{STATUS_LABEL[reservation.reservationStatus]}</span></td><td className="whitespace-nowrap px-4 py-4 text-slate-500">{formatDateTime(reservation.createdAt)}</td></tr>)}</tbody></table></div>}
      </div>
      {reservationsQuery.data && reservationsQuery.data.totalPages > 1 && <nav className="mt-8 flex items-center justify-center gap-3" aria-label="관리자 예약 페이지"><button type="button" disabled={filters.page === 0} onClick={() => setFilters((current) => ({ ...current, page: current.page - 1 }))} className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40">이전</button><span className="text-sm text-slate-600">{filters.page + 1} / {reservationsQuery.data.totalPages} · 총 {reservationsQuery.data.totalElements}건</span><button type="button" disabled={filters.page + 1 >= reservationsQuery.data.totalPages} onClick={() => setFilters((current) => ({ ...current, page: current.page + 1 }))} className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40">다음</button></nav>}
    </section>
  );
}

function FilterInput({ label, type, value, onChange }: { label: string; type: 'number' | 'date'; value: string; onChange: (value: string) => void }) { return <label className="text-sm font-medium text-slate-700">{label}<input type={type} min={type === 'number' ? 1 : undefined} value={value} onChange={(event) => onChange(event.target.value)} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2" /></label>; }
function formatDateTime(value: string) { return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value)); }
