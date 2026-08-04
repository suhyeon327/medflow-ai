import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { QueryError } from '../components/QueryError';
import { useHospitalsQuery } from '../features/hospitals/hospitalQueries';
import { HOSPITAL_DETAIL_PATH } from '../routes/routePaths';

export function HospitalListPage() {
  const [keyword, setKeyword] = useState('');
  const [submittedKeyword, setSubmittedKeyword] = useState('');
  const hospitalsQuery = useHospitalsQuery(submittedKeyword);

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmittedKeyword(keyword.trim());
  };

  const handleReset = () => {
    setKeyword('');
    setSubmittedKeyword('');
  };

  return (
    <section>
      <div>
        <p className="text-sm font-semibold text-blue-700">병원 찾기</p>
        <h1 className="mt-2 text-3xl font-bold">진료받을 병원을 찾아보세요.</h1>
        <p className="mt-3 text-slate-600">병원명, 지역 또는 주소를 통합 검색할 수 있습니다.</p>
      </div>

      <form className="mt-8 flex max-w-2xl flex-col gap-3 sm:flex-row" onSubmit={handleSearch}>
        <div className="flex-1">
          <label htmlFor="hospitalKeyword" className="sr-only">병원 검색어</label>
          <input
            id="hospitalKeyword"
            type="search"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="병원명, 지역 또는 주소"
            className="w-full rounded-md border border-slate-300 bg-white px-4 py-3 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
          />
        </div>
        <button type="submit" disabled={hospitalsQuery.isFetching} className="rounded-md bg-blue-600 px-5 py-3 font-semibold text-white hover:bg-blue-700 disabled:opacity-60">
          {hospitalsQuery.isFetching ? '검색 중...' : '검색'}
        </button>
        {(keyword || submittedKeyword) && (
          <button type="button" onClick={handleReset} className="rounded-md border border-slate-300 bg-white px-5 py-3 font-semibold text-slate-700 hover:bg-slate-100">초기화</button>
        )}
      </form>

      <div className="mt-8">
        {submittedKeyword && !hospitalsQuery.isPending && (
          <p className="mb-4 text-sm text-slate-600"><strong className="text-slate-900">‘{submittedKeyword}’</strong> 검색 결과</p>
        )}
        {hospitalsQuery.isPending && <p role="status" className="text-sm text-slate-600">병원 목록을 불러오고 있습니다.</p>}
        {hospitalsQuery.isError && <QueryError error={hospitalsQuery.error} onRetry={() => hospitalsQuery.refetch()} />}
        {hospitalsQuery.isSuccess && hospitalsQuery.data.length === 0 && (
          <p className="rounded-lg border border-slate-200 bg-white p-8 text-center text-slate-600">
            {submittedKeyword ? '검색 결과가 없습니다.' : '조회 가능한 병원이 없습니다.'}
          </p>
        )}
        {hospitalsQuery.data && hospitalsQuery.data.length > 0 && (
          <ul className="grid gap-4 sm:grid-cols-2">
            {hospitalsQuery.data.map((hospital) => (
              <li key={hospital.id}>
                <Link to={HOSPITAL_DETAIL_PATH(hospital.id)} className="block h-full rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-blue-300 hover:shadow-md">
                  <p className="text-sm font-semibold text-blue-700">{hospital.region}</p>
                  <h2 className="mt-1 font-bold text-slate-900">{hospital.name}</h2>
                  <p className="mt-3 text-sm text-slate-600">{hospital.address}</p>
                  <p className="mt-4 text-sm font-medium text-blue-700">상세 정보 보기</p>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  );
}