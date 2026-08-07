package com.medflow.auth.service;

import com.medflow.auth.dto.request.*;
import com.medflow.auth.dto.response.JwtToken;
import com.medflow.auth.dto.response.SignupResponse;
import com.medflow.auth.dto.response.WithdrawResponse;
import com.medflow.auth.jwt.JwtGenerator;
import com.medflow.auth.jwt.JwtProvider;
import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.exception.*;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import com.medflow.token.entity.RefreshToken;
import com.medflow.token.repository.RefreshTokenRepository;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtGenerator jwtGenerator;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
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

    // 로그인
    public JwtToken login(LoginRequest request) {

        try {
            // AuthenticationManager가 UserDetailsService와 PasswordEncoder를 사용해 인증 처리
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            // 인증 완료된 Authentication으로 JWT 생성
            JwtToken jwtToken = jwtGenerator.createToken(authentication);

            // Refresh Token을 DB에 저장하거나 기존 토큰을 갱신
            saveOrRenewRefreshToken(authentication.getName(), jwtToken.refreshToken());
            return jwtToken;
        } catch (AuthenticationException e) {
            // 이메일 또는 비밀번호가 틀린 경우 401 응답으로 변환
            throw new InvalidCredentialsException();
        }
    }

    // Access Token / Refresh Token 재발급
    public JwtToken reissue(ReissueRequest request) {

        String refreshTokenValue = request.refreshToken();

        // 요청으로 받은 Refresh Token이 DB에 저장되어 있는지 확인
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(InvalidCredentialsException::new);

        // JWT 자체가 유효하지 않거나 DB 기준 만료 시간이 지났으면 삭제 후 실패 처리
        if (!jwtProvider.validateToken(refreshTokenValue) || refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidCredentialsException();
        }

        // Refresh Token과 연결된 사용자 조회
        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        // 토큰 재발급을 위해 User 엔티티를 Spring Security의 Authentication으로 변환
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );

        // 새로운 Access Token과 Refresh Token 발급
        JwtToken jwtToken = jwtGenerator.createToken(authentication);

        // DB에 저장된 Refresh Token도 새 값과 새 만료 시간으로 갱신
        refreshToken.renew(
                jwtToken.refreshToken(),
                toLocalDateTime(jwtProvider.getExpiration(jwtToken.refreshToken()))
        );

        return jwtToken;
    }

    // 로그아웃
    public void logout(Long userId, LogoutRequest request) {

        // 서버에 저장된 Refresh Token을 삭제해서 이후 재발급을 막음
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(refreshToken -> {
                    if (!userId.equals(refreshToken.getUser().getId())) {
                        throw new AuthForbiddenException();
                    }
                    refreshTokenRepository.delete(refreshToken);
                });
    }

    // Refresh Token 저장 또는 갱신
    private void saveOrRenewRefreshToken(String email, String token) {

        // 인증된 사용자의 이메일로 User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        // JWT에 들어있는 만료 시간을 DB 저장 형식으로 변환
        LocalDateTime expiresAt = toLocalDateTime(jwtProvider.getExpiration(token));

        // 사용자별 Refresh Token이 이미 있으면 갱신, 없으면 새로 저장
        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        refreshToken -> refreshToken.renew(token, expiresAt),
                        () -> refreshTokenRepository.save(RefreshToken.create(user, token, expiresAt))
                );
    }

    // java.util.Date를 LocalDateTime으로 변환
    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    // 회원탈퇴
    public WithdrawResponse withdraw(Long userId, WithdrawRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 탈퇴 요청 비밀번호가 현재 사용자의 비밀번호와 일치하는지 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        user.withdraw();

        user.softDelete();

        // 환자 프로필은 선택 정보이므로 존재하는 경우에만 함께 탈퇴 처리
        patientRepository.findByUserId(userId)
                .ifPresent(Patient::softDelete);

        refreshTokenRepository.deleteByUserId(userId);

        return WithdrawResponse.from(user);
    }
}
