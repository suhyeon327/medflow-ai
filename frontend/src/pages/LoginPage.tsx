import { useState, type FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { getApiErrorMessage } from '../api/apiError';
import { useAuth } from '../auth/AuthContext';
import { ROLE_HOME_PATH, SIGNUP_PATH } from '../routes/routePaths';
import type { LoginRequest } from '../types/auth';

const INITIAL_FORM: LoginRequest = { email: '', password: '' };

export function LoginPage() {
  const [form, setForm] = useState(INITIAL_FORM);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const signupCompleted = (location.state as { signupCompleted?: boolean } | null)?.signupCompleted;

  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: (user) => navigate(ROLE_HOME_PATH[user.role], { replace: true }),
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    loginMutation.mutate(form);
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-4 py-12">
      <section className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-8">
          <p className="text-sm font-semibold text-blue-700">MedFlow AI</p>
          <h1 className="mt-2 text-2xl font-bold">로그인</h1>
          <p className="mt-2 text-sm text-slate-600">등록된 계정으로 서비스를 이용하세요.</p>
        </div>

        {signupCompleted && (
          <p role="status" className="mb-5 rounded-md bg-green-50 px-3 py-2 text-sm text-green-700">
            회원가입이 완료되었습니다. 로그인해 주세요.
          </p>
        )}

        <form className="space-y-5" onSubmit={handleSubmit}>
          <div>
            <label htmlFor="email" className="mb-2 block text-sm font-medium text-slate-700">이메일</label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              required
              maxLength={100}
              value={form.email}
              onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
              className="w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
              placeholder="name@example.com"
            />
          </div>

          <div>
            <label htmlFor="password" className="mb-2 block text-sm font-medium text-slate-700">비밀번호</label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              minLength={8}
              maxLength={20}
              value={form.password}
              onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
              className="w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
              placeholder="8~20자"
            />
          </div>

          {loginMutation.isError && (
            <p role="alert" className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
              {getApiErrorMessage(loginMutation.error)}
            </p>
          )}

          <button
            type="submit"
            disabled={loginMutation.isPending}
            className="w-full rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loginMutation.isPending ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          계정이 없나요? <Link to={SIGNUP_PATH} className="font-semibold text-blue-700">회원가입</Link>
        </p>
      </section>
    </main>
  );
}