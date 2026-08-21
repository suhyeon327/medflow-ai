import { useState } from "react";
import { QueryError } from "../components/QueryError";
import { useAdminUsersQuery } from "../features/admin/users/adminUserQueries";
import type { UserRole } from "../types/auth";
import type { AdminUserFilters, UserStatus } from "../types/adminUser";

const ROLE_LABEL: Record<UserRole, string> = {
  PATIENT: "환자",
  DOCTOR: "의사",
  ADMIN: "관리자",
};
const STATUS_LABEL: Record<UserStatus, string> = {
  ACTIVE: "활성",
  LOCKED: "잠김",
  WITHDRAWN: "탈퇴",
};
const PAGE_SIZE = 15;

function formatDate(value: string | null): string {
  if (!value) return "-";
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export function AdminUsersPage() {
  const [filters, setFilters] = useState<AdminUserFilters>({
    page: 0,
    size: PAGE_SIZE,
  });
  const usersQuery = useAdminUsersQuery(filters);

  const updateRole = (value: string) => {
    setFilters((current) => ({
      ...current,
      role: (value || undefined) as UserRole | undefined,
      page: 0,
    }));
  };

  const updateStatus = (value: string) => {
    setFilters((current) => ({
      ...current,
      status: (value || undefined) as UserStatus | undefined,
      page: 0,
    }));
  };

  return (
    <section>
      <h1 className="text-3xl font-bold">사용자 관리</h1>

      <div className="mt-8 grid max-w-2xl gap-3 rounded-xl border border-slate-200 bg-white p-5 sm:grid-cols-2">
        <label className="text-sm font-medium text-slate-700">
          역할
          <select
            value={filters.role ?? ""}
            onChange={(event) => updateRole(event.target.value)}
            className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2"
          >
            <option value="">전체 역할</option>
            {Object.entries(ROLE_LABEL).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>
        <label className="text-sm font-medium text-slate-700">
          상태
          <select
            value={filters.status ?? ""}
            onChange={(event) => updateStatus(event.target.value)}
            className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2"
          >
            <option value="">전체 상태</option>
            {Object.entries(STATUS_LABEL).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="mt-6">
        {usersQuery.isPending && (
          <p role="status" className="text-sm text-slate-600">
            사용자 목록을 불러오고 있습니다.
          </p>
        )}
        {usersQuery.isError && (
          <QueryError
            error={usersQuery.error}
            onRetry={() => usersQuery.refetch()}
          />
        )}
        {usersQuery.data?.content.length === 0 && (
          <p className="rounded-lg border border-slate-200 bg-white p-8 text-center text-slate-600">
            조건에 맞는 사용자가 없습니다.
          </p>
        )}
        {usersQuery.data && usersQuery.data.content.length > 0 && (
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
            <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
              <thead className="bg-slate-50 text-slate-600">
                <tr>
                  <th className="px-4 py-3 font-semibold">ID</th>
                  <th className="px-4 py-3 font-semibold">이메일</th>
                  <th className="px-4 py-3 font-semibold">역할</th>
                  <th className="px-4 py-3 font-semibold">상태</th>
                  <th className="px-4 py-3 font-semibold">가입일</th>
                  <th className="px-4 py-3 font-semibold">수정일</th>
                  <th className="px-4 py-3 font-semibold">탈퇴일</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {usersQuery.data.content.map((user) => (
                  <tr key={user.userId} className="hover:bg-slate-50">
                    <td className="whitespace-nowrap px-4 py-3 text-slate-500">
                      {user.userId}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-900">
                      {user.email}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3">
                      {ROLE_LABEL[user.role]}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3">
                      <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold">
                        {STATUS_LABEL[user.status]}
                      </span>
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                      {formatDate(user.createdAt)}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                      {formatDate(user.updatedAt)}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                      {formatDate(user.deletedAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {usersQuery.data && usersQuery.data.totalPages > 1 && (
        <nav
          className="mt-8 flex items-center justify-center gap-3"
          aria-label="사용자 페이지"
        >
          <button
            type="button"
            disabled={filters.page === 0}
            onClick={() =>
              setFilters((current) => ({ ...current, page: current.page - 1 }))
            }
            className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40"
          >
            이전
          </button>
          <span className="text-sm text-slate-600">
            {filters.page + 1} / {usersQuery.data.totalPages} · 총{" "}
            {usersQuery.data.totalElements}명
          </span>
          <button
            type="button"
            disabled={filters.page + 1 >= usersQuery.data.totalPages}
            onClick={() =>
              setFilters((current) => ({ ...current, page: current.page + 1 }))
            }
            className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40"
          >
            다음
          </button>
        </nav>
      )}
    </section>
  );
}
