package com.medflow.registration.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.EmailAlreadyExistsException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.common.exception.PatientAlreadyExistsException;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.registration.dto.request.DoctorSignupRequest;
import com.medflow.registration.dto.request.PatientSignupRequest;
import com.medflow.registration.dto.request.SignupRequest;
import com.medflow.registration.dto.response.SignupResponse;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;

    // 회원가입
    public SignupResponse signup(SignupRequest request) {

        // PATIENT, DOCTOR만 회원가입 허용
        if (request.role() != UserRole.PATIENT && request.role() != UserRole.DOCTOR) {
            throw new BusinessException(ErrorCode.INVALID_SIGNUP_ROLE);
        }

        // 역할과 전달된 프로필 정보 일치 여부 확인
        validateSignupProfile(request);

        // 이메일 중복 여부
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        // 의사 회원가입
        Hospital hospital = null;
        if (request.role() == UserRole.DOCTOR) {

            // 의사 요청 정보 꺼내기
            DoctorSignupRequest doctorRequest = request.doctor();

            // 병원 조회
            hospital = hospitalRepository.findById(doctorRequest.hospitalId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

            // 면허번호 중복 검사
            if (doctorRepository.existsByLicenseNumber(doctorRequest.licenseNumber())) {
                throw new BusinessException(ErrorCode.DOCTOR_ALREADY_EXISTS);
            }
        }

        // User 계정 생성
        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.role()
        );

        // 데이터베이스에 User 저장
        User savedUser = userRepository.save(user);

        // 환자인 경우
        if (request.role() == UserRole.PATIENT) {
            return signupPatient(savedUser, request.patient());
        } else {
            // 의사인 경우
            return signupDoctor(savedUser, hospital, request.doctor());
        }
    }

    // 역할과 전달된 프로필 정보 일치 여부 확인
    private void validateSignupProfile(SignupRequest request) {

        // 환자 검증
        boolean invalidPatientProfile = request.role() == UserRole.PATIENT
                && (request.patient() == null || request.doctor() != null);

        // 의사 검증
        boolean invalidDoctorProfile = request.role() == UserRole.DOCTOR
                && (request.doctor() == null || request.patient() != null);

        if (invalidPatientProfile || invalidDoctorProfile) {
            throw new BusinessException(ErrorCode.INVALID_SIGNUP_PROFILE);
        }
    }

    // 환자 회원가입
    private SignupResponse signupPatient(User user, PatientSignupRequest request) {

        // Patient 중복 확인
        if (patientRepository.existsByUserId(user.getId())) {
            throw new PatientAlreadyExistsException();
        }

        // Patient 엔티티 생성
        Patient patient = Patient.create(
                user,
                request.name(),
                request.birth(),
                request.gender(),
                request.phone().replace("-", "")
        );

        // Patient 저장
        Patient savedPatient = patientRepository.save(patient);

        return SignupResponse.from(user, savedPatient);
    }

    // 의사 회원가입
    private SignupResponse signupDoctor(
            User user,
            Hospital hospital,
            DoctorSignupRequest request
    ) {
        Doctor doctor = Doctor.create(
                user,
                hospital,
                request.name(),
                request.licenseNumber(),
                request.specialty(),
                request.introduction(),
                request.contact()
        );
        Doctor savedDoctor = doctorRepository.save(doctor);

        return SignupResponse.from(user, savedDoctor);
    }
}
