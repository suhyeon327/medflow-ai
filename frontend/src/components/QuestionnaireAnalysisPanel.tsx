import type { QuestionnaireAnalysis, QuestionnairePriority } from '../types/questionnaire';

const PRIORITY_LABEL: Record<QuestionnairePriority, string> = { NORMAL: '일반', CAUTION: '주의', HIGH_PRIORITY: '우선 확인' };

export function QuestionnaireAnalysisPanel({ analysis }: { analysis: QuestionnaireAnalysis }) {
  if (analysis.status === 'PENDING' || analysis.status === 'PROCESSING') return <p role="status" className="rounded-lg bg-blue-50 p-4 text-sm text-blue-800">AI가 문진 내용을 분석하고 있습니다. 잠시 후 자동으로 갱신됩니다.</p>;
  if (analysis.status === 'FAILED') return <p role="alert" className="rounded-lg bg-red-50 p-4 text-sm text-red-700">AI 분석을 완료하지 못했습니다.</p>;

  return <div className="space-y-5"><div className="flex flex-wrap items-center gap-3"><h2 className="text-xl font-bold">AI 문진 분석</h2><span className={`rounded-full px-3 py-1 text-xs font-bold ${analysis.priorityLevel === 'HIGH_PRIORITY' ? 'bg-red-100 text-red-800' : analysis.priorityLevel === 'CAUTION' ? 'bg-amber-100 text-amber-800' : 'bg-emerald-100 text-emerald-800'}`}>{PRIORITY_LABEL[analysis.priorityLevel]}</span></div><section className="rounded-lg bg-slate-50 p-4"><h3 className="font-semibold">요약</h3><p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-slate-700">{analysis.summary}</p></section><AnalysisList title="주요 증상" items={analysis.keyFindings} /><AnalysisList title="위험 신호" items={analysis.riskSignals} emptyText="확인된 위험 신호가 없습니다." /><AnalysisList title="의사 확인 사항" items={analysis.doctorCheckpoints} /></div>;
}

function AnalysisList({ title, items, emptyText = '내용이 없습니다.' }: { title: string; items: string[]; emptyText?: string }) { return <section><h3 className="font-semibold">{title}</h3>{items.length > 0 ? <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-slate-700">{items.map((item) => <li key={item}>{item}</li>)}</ul> : <p className="mt-2 text-sm text-slate-500">{emptyText}</p>}</section>; }
