import { Link, useParams, useSearchParams } from "react-router";
import { QueryError } from "../components/QueryError";
import { QuestionnaireAnalysisPanel } from "../components/QuestionnaireAnalysisPanel";
import { useDoctorQuestionnaireAnalysisQuery } from "../features/questionnaires/questionnaireQueries";
import { useDoctorReservationPatientQuery } from "../features/reservations/reservationQueries";
import { DOCTOR_RESERVATIONS_PATH } from "../routes/routePaths";

export function DoctorReservationDetailPage() {
  const reservationId = Number(useParams().reservationId);
  const [searchParams] = useSearchParams();
  const questionnaireId = Number(searchParams.get("questionnaireId")) || 0;
  const patientQuery = useDoctorReservationPatientQuery(
    Number.isInteger(reservationId) && reservationId > 0 ? reservationId : null,
  );
  const analysisQuery = useDoctorQuestionnaireAnalysisQuery(questionnaireId);

  if (!Number.isInteger(reservationId) || reservationId <= 0)
    return <QueryError error={new Error("올바르지 않은 예약 번호입니다.")} />;

  return (
    <section>
      <Link
        to={DOCTOR_RESERVATIONS_PATH}
        className="text-sm font-semibold text-blue-700"
      >
        ← 진료 관리
      </Link>
      <h1 className="mt-6 text-3xl font-bold">예약 상세</h1>
      <section className="mt-7 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="text-xl font-bold">환자 상세 정보</h2>
        {patientQuery.isPending && (
          <p className="mt-4 text-sm text-slate-500">
            환자 정보를 불러오고 있습니다.
          </p>
        )}
        {patientQuery.isError && (
          <div className="mt-4">
            <QueryError
              error={patientQuery.error}
              onRetry={() => patientQuery.refetch()}
            />
          </div>
        )}
        {patientQuery.data && (
          <>
            <p className="mt-2 text-sm text-slate-500">
              {formatDate(patientQuery.data.reservationDate)} ·{" "}
              {patientQuery.data.startTime.slice(0, 5)} ~{" "}
              {patientQuery.data.endTime.slice(0, 5)}
            </p>
            <dl className="mt-6 grid gap-5 sm:grid-cols-2">
              <Info label="이름" value={patientQuery.data.patientName} />
              <Info
                label="성별"
                value={patientQuery.data.gender === "MALE" ? "남성" : "여성"}
              />
              <Info label="생년월일" value={patientQuery.data.birthDate} />
            </dl>
          </>
        )}
      </section>
      <section className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="mb-5 text-xl font-bold">문진 정보</h2>
        {questionnaireId === 0 && (
          <p className="text-sm text-slate-500">작성된 문진이 없습니다.</p>
        )}
        {questionnaireId > 0 && analysisQuery.isPending && (
          <p className="text-sm text-slate-500">
            문진 분석을 불러오고 있습니다.
          </p>
        )}
        {questionnaireId > 0 && analysisQuery.isError && (
          <QueryError
            error={analysisQuery.error}
            onRetry={() => analysisQuery.refetch()}
          />
        )}
        {questionnaireId > 0 && analysisQuery.data && (
          <QuestionnaireAnalysisPanel analysis={analysisQuery.data} />
        )}
      </section>
    </section>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-sm text-slate-500">{label}</dt>
      <dd className="mt-1 font-semibold">{value}</dd>
    </div>
  );
}
function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "short",
  }).format(new Date(`${value}T00:00:00`));
}
