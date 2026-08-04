package com.medflow.auth.service;

import com.medflow.auth.dto.WithdrawRequest;
import com.medflow.auth.dto.LoginRequest;
import com.medflow.auth.jwt.JwtGenerator;
import com.medflow.auth.jwt.JwtProvider;
import com.medflow.common.exception.InvalidPasswordException;
import com.medflow.common.exception.InvalidCredentialsException;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.token.repository.RefreshTokenRepository;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import com.medflow.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtGenerator jwtGenerator;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_withUnregisteredEmail_throwsInvalidCredentialsException() {
        LoginRequest request = mock(LoginRequest.class);

        when(request.getEmail()).thenReturn("unknown@example.com");
        when(request.getPassword()).thenReturn("password123!");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new InternalAuthenticationServiceException("사용자를 찾을 수 없습니다."));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void withdraw_withMatchingPassword_withdrawsUser() {
        Long userId = 1L;
        String rawPassword = "password123!";
        String encodedPassword = "encoded-password";
        User user = User.create("patient@example.com", encodedPassword, UserRole.PATIENT);
        Patient patient = mock(Patient.class);
        WithdrawRequest request = mock(WithdrawRequest.class);

        when(request.getPassword()).thenReturn(rawPassword);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(patient));

        authService.withdraw(userId, request);

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        verify(patient).softDelete();
        verify(refreshTokenRepository).deleteByUserId(userId);
    }

    @Test
    void withdraw_withWrongPassword_throwsBeforeStateChanges() {
        Long userId = 1L;
        String rawPassword = "wrong-password";
        String encodedPassword = "encoded-password";
        User user = User.create("patient@example.com", encodedPassword, UserRole.PATIENT);
        WithdrawRequest request = mock(WithdrawRequest.class);

        when(request.getPassword()).thenReturn(rawPassword);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        assertThatThrownBy(() -> authService.withdraw(userId, request))
                .isInstanceOf(InvalidPasswordException.class);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(patientRepository, never()).findByUserId(userId);
        verify(refreshTokenRepository, never()).deleteByUserId(userId);
    }

    @Test
    void withdraw_withoutPatientProfile_withdrawsUser() {
        Long userId = 1L;
        String rawPassword = "password123!";
        String encodedPassword = "encoded-password";
        User user = User.create("patient@example.com", encodedPassword, UserRole.PATIENT);
        WithdrawRequest request = mock(WithdrawRequest.class);

        when(request.getPassword()).thenReturn(rawPassword);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.empty());

        authService.withdraw(userId, request);

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        verify(refreshTokenRepository).deleteByUserId(userId);
    }
}
