import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link } from "react-router";
import { ApiError } from "../api/apiError";
import { signup } from "../api/authApi";
import { DoctorInfoStep } from "../features/auth/signup/DoctorInfoStep";
import { PatientInfoStep } from "../features/auth/signup/PatientInfoStep";
import { SignupAccountStep } from "../features/auth/signup/SignupAccountStep";
import { SignupCompleteStep } from "../features/auth/signup/SignupCompleteStep";
import { SignupRoleStep } from "../features/auth/signup/SignupRoleStep";
import { LOGIN_PATH } from "../routes/routePaths";
import type {
  DoctorSignupForm,
  PatientSignupForm,
  SignupAccountForm,
  SignupRequest,
  SignupResponse,
  SignupRole,
} from "../types/auth";

type SignupStep = "role" | "account" | "profile" | "complete";

const INITIAL_ACCOUNT: SignupAccountForm = {
  email: "",
  password: "",
  passwordConfirm: "",
};
const INITIAL_PATIENT: PatientSignupForm = {
  name: "",
  birth: "",
  gender: "MALE",
  phone: "",
};
const INITIAL_DOCTOR: DoctorSignupForm = {
  hospitalId: null,
  name: "",
  licenseNumber: "",
  specialty: "",
  introduction: "",
  contact: "",
};

function getSignupErrorMessage(error: unknown): string {
  if (!(error instanceof ApiError))
    return "회원가입을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.";

  switch (error.code) {
    case "AUTH_001":
      return "이미 가입된 이메일입니다. 다른 이메일을 입력해 주세요.";
    case "HOSPITAL_002":
      return "선택한 병원을 찾을 수 없습니다. 병원을 다시 선택해 주세요.";
    case "DOCTOR_001":
    case "DOCTOR_004":
      return "이미 등록되었거나 인증 신청된 면허번호입니다.";
    case "AUTH_008":
      return "가입할 수 없는 회원 유형입니다.";
    case "AUTH_009":
      return "선택한 회원 유형에 맞는 추가 정보를 확인해 주세요.";
    default:
      return error.message;
  }
}

export function SignupPage() {
  const [currentStep, setCurrentStep] = useState<SignupStep>("role");
  const [selectedRole, setSelectedRole] = useState<SignupRole | null>(null);
  const [accountForm, setAccountForm] = useState(INITIAL_ACCOUNT);
  const [patientForm, setPatientForm] = useState(INITIAL_PATIENT);
  const [doctorForm, setDoctorForm] = useState(INITIAL_DOCTOR);
  const [signupResult, setSignupResult] = useState<SignupResponse | null>(null);

  const signupMutation = useMutation({
    mutationFn: signup,
    onSuccess: (result) => {
      setSignupResult(result);
      setCurrentStep("complete");
    },
  });

  const selectRole = (role: SignupRole) => {
    signupMutation.reset();
    setSelectedRole(role);
    setCurrentStep("account");
  };

  const submitSignup = () => {
    if (!selectedRole) return;

    let payload: SignupRequest;
    if (selectedRole === "PATIENT") {
      payload = {
        email: accountForm.email.trim(),
        password: accountForm.password,
        role: "PATIENT",
        patient: {
          ...patientForm,
          name: patientForm.name.trim(),
          phone: patientForm.phone.trim(),
        },
      };
    } else {
      if (!doctorForm.hospitalId) return;
      payload = {
        email: accountForm.email.trim(),
        password: accountForm.password,
        role: "DOCTOR",
        doctor: {
          hospitalId: doctorForm.hospitalId,
          name: doctorForm.name.trim(),
          licenseNumber: doctorForm.licenseNumber.trim(),
          specialty: doctorForm.specialty.trim(),
          introduction: doctorForm.introduction.trim(),
          contact: doctorForm.contact.trim(),
        },
      };
    }

    signupMutation.mutate(payload);
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-4 py-12">
      <section
        className={`w-full rounded-xl border border-slate-200 bg-white p-8 shadow-sm ${currentStep === "role" ? "max-w-2xl" : "max-w-lg"}`}
      >
        <p className="mb-7 text-sm font-bold text-blue-700">Medflow</p>

        {currentStep === "role" && <SignupRoleStep onSelect={selectRole} />}
        {currentStep === "account" && selectedRole && (
          <SignupAccountStep
            role={selectedRole}
            form={accountForm}
            onChange={setAccountForm}
            onPrevious={() => setCurrentStep("role")}
            onNext={() => {
              signupMutation.reset();
              setCurrentStep("profile");
            }}
          />
        )}
        {currentStep === "profile" && selectedRole === "PATIENT" && (
          <PatientInfoStep
            form={patientForm}
            onChange={setPatientForm}
            onPrevious={() => setCurrentStep("account")}
            onSubmit={submitSignup}
            isPending={signupMutation.isPending}
            errorMessage={
              signupMutation.isError
                ? getSignupErrorMessage(signupMutation.error)
                : ""
            }
          />
        )}
        {currentStep === "profile" && selectedRole === "DOCTOR" && (
          <DoctorInfoStep
            form={doctorForm}
            onChange={setDoctorForm}
            onPrevious={() => setCurrentStep("account")}
            onSubmit={submitSignup}
            isPending={signupMutation.isPending}
            errorMessage={
              signupMutation.isError
                ? getSignupErrorMessage(signupMutation.error)
                : ""
            }
          />
        )}
        {currentStep === "complete" && signupResult && (
          <SignupCompleteStep result={signupResult} />
        )}

        {currentStep !== "complete" && (
          <p className="mt-7 text-center text-sm text-slate-600">
            이미 계정이 있나요?{" "}
            <Link to={LOGIN_PATH} className="font-semibold text-blue-700">
              로그인
            </Link>
          </p>
        )}
      </section>
    </main>
  );
}
