import { useState, type FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { signup } from '../api/authApi';
import { getApiErrorMessage } from '../api/apiError';
import { LOGIN_PATH } from '../routes/routePaths';
import type { SignupRequest } from '../types/auth';

interface SignupForm extends SignupRequest { passwordConfirm: string; }
const INITIAL_FORM: SignupForm = {
  email: '', password: '', passwordConfirm: '', role: 'PATIENT',
};

export function SignupPage() {
  const [form, setForm] = useState(INITIAL_FORM);
  const [validationError, setValidationError] = useState('');
  const navigate = useNavigate();

  const signupMutation = useMutation({
    mutationFn: signup,
    onSuccess: () => navigate(LOGIN_PATH, {
      replace: true,
      state: { signupCompleted: true },
    }),
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setValidationError('');

    if (form.password !== form.passwordConfirm) {
      setValidationError('비밀번호가 일치하지 않습니다.');
      return;
    }

    signupMutation.mutate({ email: form.email, password: form.password, role: form.role });
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-4 py-12">
      <section className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-8">
          <p className="text-sm font-semibold text-blue-700">MedFlow AI</p>
          <h1 className="mt-2 text-2xl font-bold">회원가입</h1>
          <p className="mt-2 text-sm text-slate-600">환자 또는 의사 계정을 생성합니다.</p>
        </div>

        <form className="space-y-5" onSubmit={handleSubmit}>
          <div>
            <label htmlFor="email" className="mb-2 block text-sm font-medium">이메일</label>
            <input id="email" type="email" autoComplete="email" required maxLength={100}
              value={form.email} onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
              className="w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" />
          </div>

          <div>
            <label htmlFor="password" className="mb-2 block text-sm font-medium">비밀번호</label>
            <input id="password" type="password" autoComplete="new-password" required minLength={8} maxLength={20}
              value={form.password} onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
              className="w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" />
          </div>

          <div>
            <label htmlFor="passwordConfirm" className="mb-2 block text-sm font-medium">비밀번호 확인</label>
            <input id="passwordConfirm" type="password" autoComplete="new-password" required minLength={8} maxLength={20}
              value={form.passwordConfirm} onChange={(event) => setForm((current) => ({ ...current, passwordConfirm: event.target.value }))}
              className="w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" />
          </div>

          <fieldset>
            <legend className="mb-2 text-sm font-medium">회원 유형</legend>
            <div className="flex gap-4">
              <label className="flex items-center gap-2 text-sm"><input type="radio" name="role" value="PATIENT" checked={form.role === 'PATIENT'} onChange={() => setForm((current) => ({ ...current, role: 'PATIENT' }))} /> 환자</label>
              <label className="flex items-center gap-2 text-sm"><input type="radio" name="role" value="DOCTOR" checked={form.role === 'DOCTOR'} onChange={() => setForm((current) => ({ ...current, role: 'DOCTOR' }))} /> 의사</label>
            </div>
          </fieldset>

          {(validationError || signupMutation.isError) && (
            <p role="alert" className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
              {validationError || getApiErrorMessage(signupMutation.error)}
            </p>
          )}

          <button type="submit" disabled={signupMutation.isPending}
            className="w-full rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white hover:bg-blue-700 disabled:opacity-60">
            {signupMutation.isPending ? '가입 중...' : '회원가입'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">이미 계정이 있나요? <Link to={LOGIN_PATH} className="font-semibold text-blue-700">로그인</Link></p>
      </section>
    </main>
  );
}