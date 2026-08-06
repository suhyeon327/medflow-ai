import { useState, type FormEvent } from "react";
import type { SignupAccountForm, SignupRole } from "../../../types/auth";

interface Props {
  role: SignupRole;
  form: SignupAccountForm;
  onChange: (form: SignupAccountForm) => void;
  onPrevious: () => void;
  onNext: () => void;
}

export function SignupAccountStep({
  role,
  form,
  onChange,
  onPrevious,
  onNext,
}: Props) {
  const [error, setError] = useState("");

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    if (!/^\S+@\S+\.\S+$/.test(form.email)) {
      setError("올바른 이메일 형식을 입력해 주세요.");
      return;
    }
    if (form.password.length < 8 || form.password.length > 20) {
      setError("비밀번호는 8~20자로 입력해 주세요.");
      return;
    }
    if (form.password !== form.passwordConfirm) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }
    onNext();
  };

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      <div>
        <p className="text-sm font-semibold text-blue-700">
          {role === "PATIENT" ? "환자" : "의사"} 회원가입
        </p>
        <h1 className="mt-2 text-2xl font-bold">계정 정보를 입력해 주세요.</h1>
      </div>
      <label className="block text-sm font-medium">
        이메일
        <input
          type="email"
          autoComplete="email"
          required
          maxLength={100}
          value={form.email}
          onChange={(event) => onChange({ ...form, email: event.target.value })}
          className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
        />
      </label>
      <label className="block text-sm font-medium">
        비밀번호
        <input
          type="password"
          autoComplete="new-password"
          required
          minLength={8}
          maxLength={20}
          value={form.password}
          onChange={(event) =>
            onChange({ ...form, password: event.target.value })
          }
          className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
        />
        <span className="mt-1 block text-xs text-slate-500">
          8~20자로 입력해 주세요.
        </span>
      </label>
      <label className="block text-sm font-medium">
        비밀번호 확인
        <input
          type="password"
          autoComplete="new-password"
          required
          minLength={8}
          maxLength={20}
          value={form.passwordConfirm}
          onChange={(event) =>
            onChange({ ...form, passwordConfirm: event.target.value })
          }
          className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2.5 outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
        />
      </label>
      {error && (
        <p
          role="alert"
          className="rounded-md bg-red-50 p-3 text-sm text-red-700"
        >
          {error}
        </p>
      )}
      <div className="flex gap-3">
        <button
          type="button"
          onClick={onPrevious}
          className="flex-1 rounded-md border border-slate-300 px-4 py-2.5 font-semibold text-slate-700"
        >
          이전
        </button>
        <button
          type="submit"
          className="flex-1 rounded-md bg-blue-600 px-4 py-2.5 font-semibold text-white hover:bg-blue-700"
        >
          다음
        </button>
      </div>
    </form>
  );
}
