import { Link } from 'react-router-dom';
import { getApiErrorMessage } from '../../api/apiError';
import { useAuth } from '../../auth/AuthContext';
import { LOGIN_PATH, PATIENT_RESERVATIONS_PATH } from '../../routes/routePaths';
import { useAvailableSchedulesQuery, useCreateReservationMutation } from './reservationQueries';

function formatDate(date: string): string {
  return new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' }).format(new Date(`${date}T00:00:00`));
}

export function ScheduleBookingSection({ doctorId }: { doctorId: number }) {
  const { user } = useAuth();
  const canReserve = user?.role === 'PATIENT';
  const schedulesQuery = useAvailableSchedulesQuery(doctorId);
  const createMutation = useCreateReservationMutation(doctorId);

  return (
    <div className="mt-8 rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
      <h2 className="text-xl font-bold">예약 가능 시간</h2>
      {!user && <p className="mt-3 text-sm text-slate-600">시간은 누구나 확인할 수 있으며 예약 요청은 <Link to={LOGIN_PATH} className="font-semibold text-blue-700">환자 로그인</Link> 후 가능합니다.</p>}
      {user && !canReserve && <p className="mt-3 text-sm text-slate-600">예약 가능 시간은 확인할 수 있지만 예약 요청은 환자 계정만 가능합니다.</p>}

      <div className="mt-5">
        {schedulesQuery.isPending && <p role="status" className="text-sm text-slate-600">예약 가능 시간을 불러오고 있습니다.</p>}
        {schedulesQuery.isError && <p role="alert" className="rounded-md bg-red-50 p-3 text-sm text-red-700">{getApiErrorMessage(schedulesQuery.error)}</p>}
        {schedulesQuery.isSuccess && schedulesQuery.data.length === 0 && <p className="text-sm text-slate-600">현재 예약 가능한 시간이 없습니다.</p>}
        {schedulesQuery.data && schedulesQuery.data.length > 0 && (
          <ul className="space-y-3">
            {schedulesQuery.data.map((schedule) => (
              <li key={schedule.scheduleId} className="flex flex-col justify-between gap-3 rounded-lg border border-slate-200 p-4 sm:flex-row sm:items-center">
                <div>
                  <p className="font-semibold">{formatDate(schedule.date)}</p>
                  <p className="mt-1 text-sm text-slate-600">{schedule.startTime.slice(0, 5)} ~ {schedule.endTime.slice(0, 5)}</p>
                </div>
                {canReserve && (
                  <button type="button" disabled={createMutation.isPending} onClick={() => createMutation.mutate({ scheduleId: schedule.scheduleId })} className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-60">
                    {createMutation.isPending && createMutation.variables?.scheduleId === schedule.scheduleId ? '예약 요청 중...' : '예약 요청'}
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
        {canReserve && createMutation.isError && <p role="alert" className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{getApiErrorMessage(createMutation.error)}</p>}
        {canReserve && createMutation.isSuccess && <p role="status" className="mt-4 rounded-md bg-green-50 p-3 text-sm text-green-700">예약을 요청했습니다. <Link to={PATIENT_RESERVATIONS_PATH} className="font-semibold underline">내 예약 확인</Link></p>}
      </div>
    </div>
  );
}