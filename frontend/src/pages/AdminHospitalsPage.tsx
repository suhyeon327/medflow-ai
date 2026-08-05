import { useState, type FormEvent } from 'react';
import { getApiErrorMessage } from '../api/apiError';
import { QueryError } from '../components/QueryError';
import { useAdminHospitalsQuery, useCreateAdminHospitalMutation, useDeleteAdminHospitalMutation, useUpdateAdminHospitalMutation } from '../features/admin/hospitals/adminHospitalQueries';
import type { AdminHospital, AdminHospitalCreateRequest, HospitalStatus } from '../types/hospital';

const EMPTY_FORM: AdminHospitalCreateRequest = { name: '', address: '', region: '', tel: '' };
const STATUS_LABEL: Record<HospitalStatus, string> = { ACTIVE: '운영 중', CLOSED: '운영 종료' };

export function AdminHospitalsPage() {
  const hospitalsQuery = useAdminHospitalsQuery();
  const createMutation = useCreateAdminHospitalMutation();
  const updateMutation = useUpdateAdminHospitalMutation();
  const deleteMutation = useDeleteAdminHospitalMutation();
  const [createForm, setCreateForm] = useState(EMPTY_FORM);
  const [editingHospital, setEditingHospital] = useState<AdminHospital | null>(null);

  const handleCreate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    try {
      await createMutation.mutateAsync(createForm);
      setCreateForm(EMPTY_FORM);
    } catch {
      // 오류 메시지는 mutation 상태를 통해 화면에 표시합니다.
    }
  };

  const handleUpdate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!editingHospital) return;
    try {
      await updateMutation.mutateAsync({ hospitalId: editingHospital.id, request: { name: editingHospital.name, address: editingHospital.address, region: editingHospital.region, tel: editingHospital.tel, status: editingHospital.status } });
      setEditingHospital(null);
    } catch {
      // 오류 메시지는 mutation 상태를 통해 화면에 표시합니다.
    }
  };

  const handleDelete = (hospital: AdminHospital) => {
    if (window.confirm(`‘${hospital.name}’ 병원을 삭제하시겠습니까?`)) deleteMutation.mutate(hospital.id);
  };

  return (
    <section>
      <div><p className="text-sm font-semibold text-blue-700">관리자</p><h1 className="mt-2 text-3xl font-bold">병원 관리</h1><p className="mt-3 text-slate-600">병원을 등록하고 운영 정보와 상태를 관리합니다.</p></div>
      <form onSubmit={handleCreate} className="mt-8 rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="text-lg font-bold">새 병원 등록</h2>
        <div className="mt-4 grid gap-4 sm:grid-cols-2"><HospitalFields value={createForm} onChange={setCreateForm} prefix="create" /></div>
        {createMutation.isError && <p role="alert" className="mt-3 text-sm text-red-700">{getApiErrorMessage(createMutation.error)}</p>}
        <button type="submit" disabled={createMutation.isPending} className="mt-5 rounded-md bg-blue-600 px-5 py-2.5 font-semibold text-white hover:bg-blue-700 disabled:opacity-60">{createMutation.isPending ? '등록 중...' : '병원 등록'}</button>
      </form>
      <div className="mt-8">
        <h2 className="text-xl font-bold">등록 병원</h2>
        {hospitalsQuery.isPending && <p role="status" className="mt-5 text-sm text-slate-600">병원 목록을 불러오고 있습니다.</p>}
        {hospitalsQuery.isError && <div className="mt-5"><QueryError error={hospitalsQuery.error} onRetry={() => hospitalsQuery.refetch()} /></div>}
        {hospitalsQuery.data?.length === 0 && <p className="mt-5 rounded-lg border border-slate-200 bg-white p-8 text-center text-slate-600">등록된 병원이 없습니다.</p>}
        {hospitalsQuery.data && hospitalsQuery.data.length > 0 && <HospitalTable hospitals={hospitalsQuery.data} onEdit={setEditingHospital} onDelete={handleDelete} deleting={deleteMutation.isPending} />}
        {deleteMutation.isError && <p role="alert" className="mt-3 text-sm text-red-700">{getApiErrorMessage(deleteMutation.error)}</p>}
      </div>
      {editingHospital && <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4" role="dialog" aria-modal="true" aria-labelledby="hospital-edit-title"><form onSubmit={handleUpdate} className="max-h-full w-full max-w-2xl overflow-y-auto rounded-xl bg-white p-6 shadow-xl"><div className="flex items-center justify-between"><h2 id="hospital-edit-title" className="text-xl font-bold">병원 정보 수정</h2><button type="button" onClick={() => setEditingHospital(null)} className="rounded-md px-3 py-2 text-slate-500 hover:bg-slate-100">닫기</button></div><div className="mt-5 grid gap-4 sm:grid-cols-2"><HospitalFields value={editingHospital} onChange={(value) => setEditingHospital((current) => current ? { ...current, ...value } : null)} prefix="edit" /></div><label className="mt-4 block text-sm font-medium text-slate-700">운영 상태<select value={editingHospital.status} onChange={(event) => setEditingHospital({ ...editingHospital, status: event.target.value as HospitalStatus })} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2.5"><option value="ACTIVE">운영 중</option><option value="CLOSED">운영 종료</option></select></label>{updateMutation.isError && <p role="alert" className="mt-3 text-sm text-red-700">{getApiErrorMessage(updateMutation.error)}</p>}<div className="mt-6 flex justify-end gap-3"><button type="button" onClick={() => setEditingHospital(null)} className="rounded-md border border-slate-300 px-4 py-2.5 font-semibold">취소</button><button type="submit" disabled={updateMutation.isPending} className="rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white disabled:opacity-60">{updateMutation.isPending ? '저장 중...' : '변경 저장'}</button></div></form></div>}
    </section>
  );
}

function HospitalTable({ hospitals, onEdit, onDelete, deleting }: { hospitals: AdminHospital[]; onEdit: (hospital: AdminHospital) => void; onDelete: (hospital: AdminHospital) => void; deleting: boolean }) {
  return <div className="mt-5 overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm"><table className="min-w-full divide-y divide-slate-200 text-left text-sm"><thead className="bg-slate-50 text-slate-600"><tr><th className="px-4 py-3">병원</th><th className="px-4 py-3">지역/주소</th><th className="px-4 py-3">전화번호</th><th className="px-4 py-3">상태</th><th className="px-4 py-3 text-right">관리</th></tr></thead><tbody className="divide-y divide-slate-100">{hospitals.map((hospital) => <tr key={hospital.id} className="hover:bg-slate-50"><td className="whitespace-nowrap px-4 py-4"><strong>{hospital.name}</strong><p className="mt-1 text-xs text-slate-500">ID {hospital.id}</p></td><td className="px-4 py-4"><p className="font-medium">{hospital.region}</p><p className="mt-1 text-slate-600">{hospital.address}</p></td><td className="whitespace-nowrap px-4 py-4 text-slate-600">{hospital.tel}</td><td className="whitespace-nowrap px-4 py-4"><span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${hospital.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'}`}>{STATUS_LABEL[hospital.status]}</span></td><td className="whitespace-nowrap px-4 py-4 text-right"><button type="button" onClick={() => onEdit(hospital)} className="font-semibold text-blue-700 hover:underline">수정</button><button type="button" disabled={deleting} onClick={() => onDelete(hospital)} className="ml-4 font-semibold text-red-600 hover:underline disabled:opacity-50">삭제</button></td></tr>)}</tbody></table></div>;
}

function HospitalFields({ value, onChange, prefix }: { value: AdminHospitalCreateRequest; onChange: (value: AdminHospitalCreateRequest) => void; prefix: string }) {
  const field = (key: keyof AdminHospitalCreateRequest, label: string, placeholder: string) => <label className="text-sm font-medium text-slate-700" htmlFor={`${prefix}-${key}`}>{label}<input id={`${prefix}-${key}`} required value={value[key]} onChange={(event) => onChange({ ...value, [key]: event.target.value })} placeholder={placeholder} className="mt-2 block w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" /></label>;
  return <>{field('name', '병원명', '메드플로우 병원')}{field('region', '지역', '서울')}{field('address', '주소', '서울시 중구...')}{field('tel', '전화번호', '02-1234-5678')}</>;
}
