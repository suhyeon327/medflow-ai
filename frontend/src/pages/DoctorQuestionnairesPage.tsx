import { Link, useParams } from 'react-router-dom';
import { QueryError } from '../components/QueryError';
import { QuestionnaireAnalysisPanel } from '../components/QuestionnaireAnalysisPanel';
import { useDoctorQuestionnaireAnalysisQuery } from '../features/questionnaires/questionnaireQueries';
import { DOCTOR_RESERVATIONS_PATH } from '../routes/routePaths';

export function DoctorQuestionnairesPage() {
  const questionnaireId = Number(useParams().questionnaireId);
  const analysisQuery = useDoctorQuestionnaireAnalysisQuery(questionnaireId);

  if (!Number.isInteger(questionnaireId) || questionnaireId <= 0) return <QueryError error={new Error('올바르지 않은 문진 번호입니다.')} />;

  return <section><Link to={DOCTOR_RESERVATIONS_PATH} className="text-sm font-semibold text-blue-700">← 예약 관리</Link><div className="mt-6"><p className="text-sm font-semibold text-blue-700">진료 지원</p><h1 className="mt-2 text-3xl font-bold">AI 문진 분석</h1><p className="mt-3 text-slate-600">담당 예약에 작성된 문진의 AI 분석 결과입니다.</p></div><section className="mt-8 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">{analysisQuery.isPending && <p className="text-sm text-slate-600">AI 분석 결과를 불러오고 있습니다.</p>}{analysisQuery.isError && <QueryError error={analysisQuery.error} onRetry={() => analysisQuery.refetch()} />}{analysisQuery.data && <QuestionnaireAnalysisPanel analysis={analysisQuery.data} />}</section></section>;
}
