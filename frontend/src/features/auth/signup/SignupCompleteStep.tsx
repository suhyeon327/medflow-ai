import { Link } from 'react-router-dom';
import { LOGIN_PATH } from '../../../routes/routePaths';
import type { SignupResponse } from '../../../types/auth';

export function SignupCompleteStep({ result }: { result: SignupResponse }) {
  const isDoctor = result.role === 'DOCTOR';

  return (
    <div className="text-center">
      <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-green-100 text-2xl text-green-700">✓</div>
      <h1 className="mt-5 text-2xl font-bold">{isDoctor ? '의사 회원가입 및 인증 신청이 완료되었습니다.' : '회원가입이 완료되었습니다.'}</h1>
      <p className="mt-3 text-sm leading-6 text-slate-600">
        {isDoctor ? '관리자 승인 후 의사 기능을 이용할 수 있습니다.' : '로그인 후 병원을 검색하고 예약할 수 있습니다.'}
      </p>
      {isDoctor && <p className="mt-5 rounded-md bg-amber-50 px-4 py-3 text-sm font-semibold text-amber-800">인증 상태: {result.profileStatus ?? 'PENDING'}</p>}
      <Link to={LOGIN_PATH} className="mt-7 inline-block w-full rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white hover:bg-blue-700">로그인하기</Link>
    </div>
  );
}