import { useState, type FormEvent } from "react";
import { Link } from "react-router";
import { QueryError } from "../components/QueryError";
import {
  useHospitalSummaryQuery,
  useHospitalsQuery
} from "../features/hospitals/hospitalQueries";
import { HOSPITAL_DETAIL_PATH } from "../routes/routePaths";

export function HospitalListPage() {
  const [keyword, setKeyword] = useState("");
  const [submittedKeyword, setSubmittedKeyword] = useState("");
  const [page, setPage] = useState(0);
  
  // 병원 API 호출
  const hospitalsQuery = useHospitalsQuery(
    submittedKeyword,
    page
  );

  // 전체 병원 및 의료진 통계 조회
  const summaryQuery = useHospitalSummaryQuery();

  // 현재 페이지 병원 목록 추출
  const hospitals = hospitalsQuery.data?.content ?? [];

  // 검색 처리 함수
  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();   // 브라우저의 기본 form 제출 동작 막기
    setSubmittedKeyword(keyword.trim());   // 사용자가 검색창에 입력한 값을 실제 검색어로 저장
    setPage(0);   // 검색 후 첫 페이지로 돌아감
  };

  return (
    <section>
      <div className="border-b border-blue-100 bg-gradient-to-br from-blue-50 via-white to-sky-100">
        <div className="mx-auto max-w-7xl px-6 py-14 lg:py-20">
          <h1 className="max-w-2xl text-4xl font-extrabold leading-tight tracking-tight text-slate-900 lg:text-5xl">
            원하는 병원을
            <br />
            <span className="text-blue-600">쉽고 빠르게</span> 찾아보세요.
          </h1>

          <form
            onSubmit={handleSearch}
            className="mt-10 flex max-w-5xl flex-col gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-xl shadow-blue-100/60 sm:flex-row sm:items-center"
          >
            <div className="flex min-w-0 flex-1 items-center gap-3 px-3">
              <span className="text-2xl text-blue-600">⌖</span>

              <input
                id="hospitalKeyword"
                aria-label="병원 검색"   // 스크린 리더에게 용도를 알려주는 역할
                type="search"
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
                placeholder="병원명 또는 주소를 입력하세요"
                className="w-full border-0 p-0 text-sm text-slate-700 outline-none"
              />
            </div>

            <button
              type="submit"
              disabled={hospitalsQuery.isFetching}   // API 요청 중에는 비활성화
              className="rounded-xl bg-blue-600 px-10 py-4 font-bold text-white shadow-md hover:bg-blue-700 disabled:opacity-60"
            >
              {hospitalsQuery.isFetching ? "검색 중" : "검색하기"}
            </button>
          </form>
        </div>
      </div>

      <div className="mx-auto max-w-7xl px-6 py-10">
        <div className="mb-10 grid overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm sm:grid-cols-3">
          <Summary
            icon="▦"
            label="등록 병원"
            value={`${summaryQuery.data?.hospitalCount ?? 0}개`}
          />

          <Summary
            icon="♙"
            label="전문 의료진"
            value={`${summaryQuery.data?.doctorCount ?? 0}명`}
          />

          <Summary
            icon="▣"
            label="간편 예약"
            value="24시간"
          />
        </div>

        <div className="mb-5 flex items-end justify-between">
          <h2 className="text-2xl font-extrabold">병원 목록</h2>

          {submittedKeyword && (
            <p className="text-sm text-slate-500">
              ‘{submittedKeyword}’ 검색 결과
            </p>
          )}
        </div>

        {hospitalsQuery.isPending && (
          <p role="status" className="py-16 text-center text-slate-500">
            병원 목록을 불러오고 있습니다.
          </p>
        )}

        {hospitalsQuery.isError && (
          <QueryError
            error={hospitalsQuery.error}
            onRetry={() => hospitalsQuery.refetch()}
          />
        )}

        {hospitalsQuery.isSuccess && hospitals.length === 0 && (
          <p className="rounded-2xl border border-slate-200 bg-white p-12 text-center text-slate-600">
            {submittedKeyword
              ? "검색 결과가 없습니다."
              : "조회 가능한 병원이 없습니다."}
          </p>
        )}

        {hospitals.length > 0 && (
          <ul className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {hospitals.map((hospital) => {
              const specialties = hospital.specialties;

              return (
                <li
                  key={hospital.id}
                  className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-lg"
                >
                  <p className="text-sm font-bold text-blue-600">
                    {hospital.region}
                  </p>

                  <h3 className="mt-2 text-xl font-extrabold text-slate-900">
                    {hospital.name}
                  </h3>

                  <div className="mt-4 flex min-h-7 flex-wrap gap-2">
                    {specialties.length > 0 ? (
                      specialties.map((specialty) => (
                        <span
                          key={specialty}
                          className="rounded-full bg-blue-50 px-2.5 py-1 text-xs font-semibold text-blue-700"
                        >
                          {specialty}
                        </span>
                      ))
                    ) : (
                      <span className="text-sm text-slate-400">
                        진료과 정보 없음
                      </span>
                    )}
                  </div>

                  <p className="mt-4 text-sm text-slate-500">전화번호</p>

                  <p className="mt-1 font-semibold text-slate-800">
                    {hospital.tel}
                  </p>

                  <Link
                    to={HOSPITAL_DETAIL_PATH(hospital.id)}
                    className="mt-6 block rounded-lg border border-blue-200 py-2.5 text-center text-sm font-bold text-blue-600 hover:bg-blue-50"
                  >
                    병원 정보 보기 ›
                  </Link>
                </li>
              );
            })}
          </ul>
        )}

        {hospitalsQuery.data && hospitalsQuery.data.totalPages > 1 && (
          <nav
            className="mt-8 flex items-center justify-center gap-3"
            aria-label="병원 페이지"
          >
            <button
              type="button"
              disabled={
                hospitalsQuery.data.first ||
                hospitalsQuery.isFetching
              }
              onClick={() => setPage((current) => current - 1)}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40"
            >
              이전
            </button>

            <span className="text-sm text-slate-600">
              {hospitalsQuery.data.page + 1} /{" "}
              {hospitalsQuery.data.totalPages}
            </span>

            <button
              type="button"
              disabled={
                hospitalsQuery.data.last ||
                hospitalsQuery.isFetching
              }
              onClick={() => setPage((current) => current + 1)}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm disabled:opacity-40"
            >
              다음
            </button>
          </nav>
        )}
      </div>
    </section>
  );
}

function Summary({
  icon,
  label,
  value,
}: {
  icon: string;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-center justify-center gap-4 border-b border-slate-200 p-6 last:border-0 sm:border-b-0 sm:border-r">
      <span className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-50 text-2xl text-blue-600">
        {icon}
      </span>
      <div>
        <p className="text-sm text-slate-500">{label}</p>
        <p className="text-2xl font-extrabold text-slate-900">{value}</p>
      </div>
    </div>
  );
}
