import { useState, type FormEvent } from 'react';
import { getApiErrorMessage } from '../api/apiError';
import { useCreateDoctorSchedulesMutation, useOwnDoctorSchedulesQuery } from '../features/doctors/doctorQueries';

function getLocalDateString(date = new Date()): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

const TODAY = getLocalDateString();

export function DoctorSchedulesPage() {
  const [filterDate, setFilterDate] = useState(TODAY);
  const [form, setForm] = useState({ date: TODAY, startTime: '09:00', endTime: '18:00', slotMinutes: 30 });
  const schedulesQuery = useOwnDoctorSchedulesQuery(filterDate);
  const createMutation = useCreateDoctorSchedulesMutation(filterDate);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (form.startTime >= form.endTime) return;
    createMutation.mutate({ ...form, startTime: `${form.startTime}:00`, endTime: `${form.endTime}:00` });
  };

  return (
    <section>
      <div><p className="text-sm font-semibold text-blue-700">진료 관리</p><h1 className="mt-2 text-3xl font-bold">내 진료 스케줄</h1></div>
      <div className="mt-8 grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.3fr)]">
        <form onSubmit={submit} className="space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-lg font-bold">스케줄 생성</h2>
          <label className="block text-sm font-medium">진료일<input type="date" required value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2" /></label>
          <div className="grid grid-cols-2 gap-3"><label className="text-sm font-medium">시작 시간<input type="time" required value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2" /></label><label className="text-sm font-medium">종료 시간<input type="time" required value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2" /></label></div>
          <label className="block text-sm font-medium">진료 간격<select value={form.slotMinutes} onChange={(e) => setForm({ ...form, slotMinutes: Number(e.target.value) })} className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2">{[10, 15, 20, 30, 40, 50, 60].map((minute) => <option key={minute} value={minute}>{minute}분</option>)}</select></label>
          {form.startTime >= form.endTime && <p className="text-sm text-red-700">종료 시간은 시작 시간보다 늦어야 합니다.</p>}
          {createMutation.isError && <p role="alert" className="rounded-md bg-red-50 p-3 text-sm text-red-700">{getApiErrorMessage(createMutation.error)}</p>}
          {createMutation.isSuccess && <p role="status" className="rounded-md bg-green-50 p-3 text-sm text-green-700">{createMutation.data.length}개의 진료 시간이 생성되었습니다.</p>}
          <button disabled={createMutation.isPending || form.startTime >= form.endTime} className="w-full rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white disabled:opacity-60">{createMutation.isPending ? '생성 중...' : '스케줄 생성'}</button>
        </form>
        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex flex-wrap items-end justify-between gap-3"><h2 className="text-lg font-bold">등록된 스케줄</h2><label className="text-sm">날짜 필터<input type="date" value={filterDate} onChange={(e) => setFilterDate(e.target.value || TODAY)} className="ml-2 rounded-md border border-slate-300 px-3 py-2" /></label></div>
          {schedulesQuery.isPending && <p className="mt-5 text-sm text-slate-600">스케줄을 불러오고 있습니다.</p>}
          {schedulesQuery.isError && <p className="mt-5 text-sm text-red-700">{getApiErrorMessage(schedulesQuery.error)}</p>}
          {schedulesQuery.data?.length === 0 && <p className="mt-5 text-sm text-slate-600">등록된 스케줄이 없습니다.</p>}
          <ul className="mt-5 space-y-3">{schedulesQuery.data?.map((schedule) => <li key={schedule.scheduleId} className="rounded-lg border border-slate-200 p-4"><p className="font-semibold">{schedule.date}</p><p className="mt-1 text-sm text-slate-600">{schedule.startTime.slice(0, 5)} ~ {schedule.endTime.slice(0, 5)}</p></li>)}</ul>
        </div>
      </div>
    </section>
  );
}