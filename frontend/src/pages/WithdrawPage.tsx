import { useState, type FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "../api/apiError";
import { useAuth } from "../auth/AuthContext";
import { LOGIN_PATH, ROLE_HOME_PATH } from "../routes/routePaths";

export function WithdrawPage() {
  const [password, setPassword] = useState("");
  const { withdraw } = useAuth();
  const navigate = useNavigate();

  const withdrawMutation = useMutation({
    mutationFn: withdraw,
    onSuccess: () =>
      navigate(LOGIN_PATH, {
        replace: true,
        state: { withdrawCompleted: true },
      }),
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    withdrawMutation.mutate(password);
  };

  return (
    <section className="mx-auto max-w-lg rounded-xl border border-red-200 bg-white p-8 shadow-sm">
      <p className="text-sm font-semibold text-red-600">계정 관리</p>
      <h1 className="mt-2 text-2xl font-bold">회원 탈퇴</h1>
      <div className="mt-5 rounded-md bg-red-50 p-4 text-sm leading-6 text-red-800">
        탈퇴하면 계정과 환자 정보가 삭제 처리되며 현재 로그인도 종료됩니다. 이
        작업은 되돌릴 수 없습니다.
      </div>

      <form className="mt-6 space-y-5" onSubmit={handleSubmit}>
        <div>
          <label
            htmlFor="withdrawPassword"
            className="mb-2 block text-sm font-medium text-slate-700"
          >
            현재 비밀번호
          </label>
          <input
            id="withdrawPassword"
            type="password"
            autoComplete="current-password"
            required
            minLength={8}
            maxLength={20}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-red-600 focus:ring-2 focus:ring-red-100"
          />
        </div>

        {withdrawMutation.isError && (
          <p
            role="alert"
            className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700"
          >
            {getApiErrorMessage(withdrawMutation.error)}
          </p>
        )}

        <div className="flex gap-3">
          <Link
            to={ROLE_HOME_PATH.PATIENT}
            className="flex-1 rounded-md border border-slate-300 px-4 py-2.5 text-center font-semibold text-slate-700 hover:bg-slate-100"
          >
            취소
          </Link>
          <button
            type="submit"
            disabled={withdrawMutation.isPending}
            className="flex-1 rounded-md bg-red-600 px-4 py-2.5 font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {withdrawMutation.isPending ? "탈퇴 처리 중..." : "회원 탈퇴"}
          </button>
        </div>
      </form>
    </section>
  );
}
