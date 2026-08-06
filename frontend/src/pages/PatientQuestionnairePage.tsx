import { useEffect, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiError, getApiErrorMessage } from '../api/apiError';
import { QueryError } from '../components/QueryError';
import { QuestionnaireAnalysisPanel } from '../components/QuestionnaireAnalysisPanel';
import { useCreateQuestionnaireMutation, useQuestionnaireAnalysisQuery, useQuestionnaireQuery, useUpdateQuestionnaireMutation } from '../features/questionnaires/questionnaireQueries';
import { PATIENT_RESERVATIONS_PATH } from '../routes/routePaths';
import type { QuestionnaireFormData } from '../types/questionnaire';

const EMPTY_FORM: QuestionnaireFormData = { chiefComplaint: '', symptomStartedAt: '', symptomDescription: '', painLevel: null, temperature: null, associatedSymptoms: '', medicalHistory: '', medications: '', allergies: '', additionalNote: '' };

export function PatientQuestionnairePage() {
  const reservationId = Number(useParams().reservationId);
  const questionnaireQuery = useQuestionnaireQuery(reservationId);
  const createMutation = useCreateQuestionnaireMutation();
  const updateMutation = useUpdateQuestionnaireMutation(reservationId);
  const [form, setForm] = useState(EMPTY_FORM);
  const questionnaire = questionnaireQuery.data;
  const notCreated = questionnaireQuery.error instanceof ApiError && questionnaireQuery.error.status === 404;
  const analysisQuery = useQuestionnaireAnalysisQuery(questionnaire?.questionnaireId ?? null);

  useEffect(() => {
    if (questionnaire) setForm({ chiefComplaint: questionnaire.chiefComplaint, symptomStartedAt: questionnaire.symptomStartedAt.slice(0, 16), symptomDescription: questionnaire.symptomDescription, painLevel: questionnaire.painLevel, temperature: questionnaire.temperature, associatedSymptoms: questionnaire.associatedSymptoms ?? '', medicalHistory: questionnaire.medicalHistory ?? '', medications: questionnaire.medications ?? '', allergies: questionnaire.allergies ?? '', additionalNote: questionnaire.additionalNote ?? '' });
  }, [questionnaire]);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    try {
      if (questionnaire) await updateMutation.mutateAsync({ questionnaireId: questionnaire.questionnaireId, request: form });
      else await createMutation.mutateAsync({ ...form, reservationId });
    } catch {
      // 오류 메시지는 mutation 상태를 통해 화면에 표시합니다.
    }
  };
  const mutation = questionnaire ? updateMutation : createMutation;

  if (!Number.isInteger(reservationId) || reservationId <= 0) return <QueryError error={new Error('올바르지 않은 예약 번호입니다.')} />;

  return <section><Link to={PATIENT_RESERVATIONS_PATH} className="text-sm font-semibold text-blue-700">← 내 예약</Link><div className="mt-6"><p className="text-sm font-semibold text-blue-700">예약 문진</p><h1 className="mt-2 text-3xl font-bold">진료 전 문진 작성</h1><p className="mt-3 text-slate-600">현재 증상과 건강 정보를 작성하면 의료진의 진료 준비에 활용됩니다.</p></div>{questionnaireQuery.isPending && <p className="mt-8 text-sm text-slate-600">문진 정보를 확인하고 있습니다.</p>}{questionnaireQuery.isError && !notCreated && <div className="mt-8"><QueryError error={questionnaireQuery.error} onRetry={() => questionnaireQuery.refetch()} /></div>}{(questionnaire || notCreated) && <form onSubmit={submit} className="mt-8 space-y-5 rounded-xl border border-slate-200 bg-white p-6 shadow-sm"><div className="grid gap-5 sm:grid-cols-2"><Field label="주 증상" required><input required value={form.chiefComplaint} onChange={(event) => setForm({ ...form, chiefComplaint: event.target.value })} className={INPUT_CLASS} placeholder="예: 두통과 발열" /></Field><Field label="증상 시작 시점" required><input type="datetime-local" required value={form.symptomStartedAt} onChange={(event) => setForm({ ...form, symptomStartedAt: event.target.value })} className={INPUT_CLASS} /></Field></div><Field label="증상 상세 설명" required><textarea required rows={4} value={form.symptomDescription} onChange={(event) => setForm({ ...form, symptomDescription: event.target.value })} className={INPUT_CLASS} /></Field><div className="grid gap-5 sm:grid-cols-2"><Field label="통증 정도 (0~10)"><input type="number" min="0" max="10" value={form.painLevel ?? ''} onChange={(event) => setForm({ ...form, painLevel: event.target.value ? Number(event.target.value) : null })} className={INPUT_CLASS} /></Field><Field label="체온 (℃)"><input type="number" min="30" max="45" step="0.1" value={form.temperature ?? ''} onChange={(event) => setForm({ ...form, temperature: event.target.value ? Number(event.target.value) : null })} className={INPUT_CLASS} /></Field></div><div className="grid gap-5 sm:grid-cols-2"><TextField label="동반 증상" value={form.associatedSymptoms} onChange={(value) => setForm({ ...form, associatedSymptoms: value })} /><TextField label="과거 병력" value={form.medicalHistory} onChange={(value) => setForm({ ...form, medicalHistory: value })} /><TextField label="복용 중인 약" value={form.medications} onChange={(value) => setForm({ ...form, medications: value })} /><TextField label="알레르기" value={form.allergies} onChange={(value) => setForm({ ...form, allergies: value })} /></div><Field label="추가 전달 사항"><textarea rows={3} value={form.additionalNote} onChange={(event) => setForm({ ...form, additionalNote: event.target.value })} className={INPUT_CLASS} /></Field>{mutation.isError && <p role="alert" className="rounded-md bg-red-50 p-3 text-sm text-red-700">{getApiErrorMessage(mutation.error)}</p>}{mutation.isSuccess && <p role="status" className="rounded-md bg-emerald-50 p-3 text-sm text-emerald-700">문진이 {questionnaire ? '수정' : '등록'}되었습니다.</p>}<button type="submit" disabled={mutation.isPending} className="rounded-md bg-blue-600 px-5 py-2.5 font-semibold text-white hover:bg-blue-700 disabled:opacity-60">{mutation.isPending ? '저장 중...' : questionnaire ? '문진 수정' : '문진 등록'}</button></form>}{questionnaire && <section className="mt-8 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">{analysisQuery.isPending && <p className="text-sm text-slate-600">AI 분석 결과를 불러오고 있습니다.</p>}{analysisQuery.isError && <QueryError error={analysisQuery.error} onRetry={() => analysisQuery.refetch()} />}{analysisQuery.data && <QuestionnaireAnalysisPanel analysis={analysisQuery.data} />}</section>}</section>;
}

const INPUT_CLASS = 'mt-2 block w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100';
function Field({ label, required, children }: { label: string; required?: boolean; children: React.ReactNode }) { return <label className="block text-sm font-medium text-slate-700">{label}{required && <span className="ml-1 text-red-600">*</span>}{children}</label>; }
function TextField({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) { return <Field label={label}><textarea rows={2} value={value} onChange={(event) => onChange(event.target.value)} className={INPUT_CLASS} /></Field>; }
