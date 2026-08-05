import { useState } from 'react';
import { Link } from 'react-router-dom';
import { QueryError } from '../components/QueryError';
import { useAdminUsersQuery } from '../features/admin/users/adminUserQueries';
import { useAdminDoctorsQuery } from '../features/doctors/doctorQueries';
import type { UserRole } from '../types/auth';
import type { AdminUserFilters, UserStatus } from '../types/adminUser';
import type { DoctorStatus } from '../types/doctor';
import { ADMIN_DOCTOR_DETAIL_PATH } from '../routes/routePaths';

const ROLE_LABEL: Record<UserRole, string> = { PATIENT: '환자', DOCTOR: '의사', ADMIN: '관리자' };
const STATUS_LABEL: Record<UserStatus, string> = { ACTIVE: '활성', LOCKED: '잠김', WITHDRAWN: '탈퇴' };
const DOCTOR_STATUS_LABEL: Record<DoctorStatus, string> = { PENDING: '승인 대기', ACTIVE: '승인 완료', REJECTED: '승인 거절' };
const PAGE_SIZE = 20;

function formatDate(value: string | null): string {
  if (!value) return '-';
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
}

export function AdminUsersPage() {
  const [filters, setFilters] = useState<AdminUserFilters>({ page: 0, size: PAGE_SIZE });
  const usersQuery = useAdminUsersQuery(filters);
  const [doctorStatus, setDoctorStatus] = useState<DoctorStatus | undefined>('PENDING');
  const doctorsQuery = useAdminDoctorsQuery(doctorStatus);

  const updateRole = (value: string) => {
    setFilters((current) => ({ ...current, role: (value || undefined) as UserRole | undefined, page: 0 }));
  };

  const updateStatus = (value: string) => {
    setFilters((current) => ({ ...current, status: (value || undefined) as UserStatus | undefined, page: 0 }));
  };

  return (
    <section>
      <div>
        <p className="text-sm font-semibold text-blue-700">관리자</p>
        <h1 className="mt-2 text-3xl font-bold">사용자 관리</h1>
        <p className="mt-3 text-slate-600">역할과 계정 상태를 기준으로 사용자를 조회합니다.</p>
      </div>

      <div className="mt-8 grid max-w-2xl gap-3 rounded-xl border border-slate-200 bg-white p-5 sm:grid-cols-2">
        <label className="text-sm font-medium text-slate-700">역할
          <select value={filters.role ?? ''} onChange={(event) => updateRole(event.target.value)} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2">
            <option value="">전체 역할</option>
            {Object.entries(ROLE_LABEL).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </label>
        <label className="text-sm font-medium text-slate-700">상태
          <select value={filters.status ?? ''} onChange={(event) => updateStatus(event.target.value)} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2">
            <option value="">전체 상태</option>
            {Object.entries(STATUS_LABEL).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </label>
      </div>

      <section className="mt-8 rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex flex-wrap items-end justify-between gap-3">
          <div><h2 className="text-lg font-bold">의사 인증 목록</h2><p className="mt-1 text-sm text-slate-600">의사를 선택하면 상세 정보와 승인 상태를 확인할 수 있습니다.</p></div>
          <label className="text-sm font-medium text-slate-700">승인 상태
            <select value={doctorStatus ?? ''} onChange={(event) => setDoctorStatus((event.target.value || undefined) as DoctorStatus | undefined)} className="ml-2 rounded-md border border-slate-300 px-3 py-2">
              <option value="">전체</option>
              {Object.entries(DOCTOR_STATUS_LABEL).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </label>
        </div>
        {doctorsQuery.isPending && <p className="mt-5 text-sm text-slate-600">의사 목록을 불러오고 있습니다.</p>}
        {doctorsQuery.isError && <div className="mt-5"><QueryError error={doctorsQuery.error} onRetry={() => doctorsQuery.refetch()} /></div>}
        {doctorsQuery.data?.length === 0 && <p className="mt-5 text-sm text-slate-600">해당 상태의 의사가 없습니다.</p>}
        <ul className="mt-5 grid gap-3 sm:grid-cols-2">{doctorsQuery.data?.map((doctor) => (
          <li key={doctor.doctorId}><Link to={ADMIN_DOCTOR_DETAIL_PATH(doctor.doctorId)} className="block rounded-lg border border-slate-200 p-4 hover:border-blue-300 hover:bg-blue-50"><div className="flex items-center justify-between gap-3"><strong>{doctor.doctorName} 의사</strong><span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold">{DOCTOR_STATUS_LABEL[doctor.status]}</span></div><p className="mt-2 text-sm text-slate-600">{doctor.hospitalName}</p><p className="mt-1 text-xs text-slate-500">면허번호 {doctor.licenseNumber}</p></Link></li>
        ))}</ul>
      </section>
      <div className="mt-6">
        {usersQuery.isPending && <p role="status" className="text-sm text-slate-600">사용자 목록을 불러오고 있습니다.</p>}
        {usersQuery.isError && <QueryError error={usersQuery.error} onRetry={() => usersQuery.refetch()} />}
        {usersQuery.data?.content.length === 0 && <p className="rounded-lg border border-slate-200 bg-white p-8 text-center text-slate-600">조건에 맞는 사용자가 없습니다.</p>}
        {usersQuery.data && usersQuery.data.content.length > 0 && (
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
            <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
              <thead className="bg-slate-50 text-slate-600">
                <tr><th className="px-4 py-3 font-semibold">ID</th><th className="px-4 py-3 font-semibold">이메일</th><th className="px-4 py-3 font-semibold">역할</th><th className="px-4 py-3 font-semibold">상태</th><th className="px-4 py-3 font-semibold">가입일</th><th className="px-4 py-3 font-semibold">수정일</th><th className="px-4 py-3 font-semibold">탈퇴일</th></tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {usersQuery.data.content.map((user) => (
                  <tr key={user.userId} className="hover:bg-slate-50">
                    <td className="whitespace-nowrap px-4 py-3 text-slate-500">{user.userId}</td>
                    <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-900">{user.email}</td>
                    <td className="whitespace-nowrap px-4 py-3">{ROLE_LABEL[user.role]}</td>
                    <td className="whitespace-nowrap px-4 py-3"><span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold">{STATUS_LABEL[user.status]}</span></td>
                    <td className="whitespace-nowrap px-4 py-3 text-slate-600">{formatDate(user.createdAt)}</td>
                    <td className="whitespace-nowrap px-4 py-3 text-slate-600">{formatDate(user.updatedAt)}</td>
                    <td className="whitespace-nowrap px-4 py-3 text-slate-600">{formatDate(user.deletedAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {usersQuery.data && usersQuery.data.totalPages > 1 && (
        <nav className="mt-8 flex items-center justify-center gap-3" aria-label="사용자 페이지">
          <button type="button" disabled={filters.page === 0} onClick={() => setFilters((current) => ({ ...current, page: current.page - 1 }))} className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40">이전</button>
          <span className="text-sm text-slate-600">{filters.page + 1} / {usersQuery.data.totalPages} · 총 {usersQuery.data.totalElements}명</span>
          <button type="button" disabled={filters.page + 1 >= usersQuery.data.totalPages} onClick={() => setFilters((current) => ({ ...current, page: current.page + 1 }))} className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40">다음</button>
        </nav>
      )}
    </section>
  );
}