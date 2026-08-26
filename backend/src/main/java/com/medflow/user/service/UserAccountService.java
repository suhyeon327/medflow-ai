package com.medflow.user.service;

import com.medflow.auth.token.service.RefreshTokenService;
import com.medflow.common.exception.InvalidPasswordException;
import com.medflow.common.exception.UserNotFoundException;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.user.dto.request.WithdrawRequest;
import com.medflow.user.dto.response.WithdrawResponse;
import com.medflow.user.entity.User;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final RefreshTokenService refreshTokenService;

    // 회원탈퇴
    public WithdrawResponse withdraw(Long userId, WithdrawRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 탈퇴 요청 비밀번호가 현재 사용자의 비밀번호와 일치하는지 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        user.withdraw();

        // 환자 프로필은 선택 정보이므로 존재하는 경우에만 함께 탈퇴 처리
        patientRepository.findByUserId(userId)
                .ifPresent(Patient::softDelete);

        // 의사 프로필도 존재하는 경우 함께 탈퇴 처리
        doctorRepository.findByUserId(userId)
                .ifPresent(Doctor::softDelete);

        refreshTokenService.revokeAll(userId);

        return WithdrawResponse.from(user);
    }
}
