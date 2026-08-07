package com.medflow.auth.service;

import com.medflow.auth.dto.request.WithdrawRequest;
import com.medflow.auth.dto.request.LoginRequest;
import com.medflow.auth.dto.request.LogoutRequest;
import com.medflow.auth.dto.request.ReissueRequest;
import com.medflow.auth.dto.request.SignupRequest;
import com.medflow.auth.dto.response.JwtToken;
import com.medflow.auth.dto.response.SignupResponse;
import com.medflow.auth.dto.request.PatientSignupRequest;
import com.medflow.auth.dto.request.DoctorSignupRequest;
import com.medflow.auth.jwt.JwtGenerator;
import com.medflow.auth.jwt.JwtProvider;
import com.medflow.common.exception.InvalidPasswordException;
import com.medflow.common.exception.InvalidCredentialsException;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.EmailAlreadyExistsException;
import com.medflow.common.exception.AuthForbiddenException;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.token.repository.RefreshTokenRepository;
import com.medflow.token.entity.RefreshToken;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import com.medflow.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_patient_savesUserAndPatient() {
        SignupRequest request = patientSignupRequest();
        PatientSignupRequest patientRequest = request.patient();
        User savedUser = mock(User.class);
        Patient savedPatient = mock(Patient.class);

        when(passwordEncoder.encode("password123!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(savedUser.getId()).thenReturn(1L);
        when(savedUser.getEmail()).thenReturn("patient@example.com");
        when(savedUser.getRole()).thenReturn(UserRole.PATIENT);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        when(savedPatient.getId()).thenReturn(10L);

        SignupResponse response = authService.signup(request);

        assertThat(response.email()).isEqualTo("patient@example.com");
        assertThat(response.role()).isEqualTo(UserRole.PATIENT);
        assertThat(response.profileId()).isEqualTo(10L);
        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(patientCaptor.capture());
        assertThat(patientCaptor.getValue().getPhone()).isEqualTo("01012345678");
        verify(patientRequest).name();
    }

    @Test
    void signup_doctor_savesPendingDoctor() {
        SignupRequest request = doctorSignupRequest();
        User savedUser = mock(User.class);
        Hospital hospital = mock(Hospital.class);

        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(passwordEncoder.encode("password123!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(savedUser.getId()).thenReturn(2L);
        when(savedUser.getEmail()).thenReturn("doctor@example.com");
        when(savedUser.getRole()).thenReturn(UserRole.DOCTOR);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SignupResponse response = authService.signup(request);

        assertThat(response.email()).isEqualTo("doctor@example.com");
        assertThat(response.role()).isEqualTo(UserRole.DOCTOR);
        assertThat(response.profileStatus()).isEqualTo(DoctorStatus.PENDING);
        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(doctorCaptor.capture());
        assertThat(doctorCaptor.getValue().getStatus()).isEqualTo(DoctorStatus.PENDING);
    }

    @Test
    void signup_withDuplicatedEmail_throwsException() {
        SignupRequest request = patientSignupRequest();
        when(userRepository.existsByEmail("patient@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void signup_withAdminRole_throwsException() {
        SignupRequest request = mock(SignupRequest.class);
        when(request.role()).thenReturn(UserRole.ADMIN);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(com.medflow.common.exception.ErrorCode.INVALID_SIGNUP_ROLE);
    }

    @Test
    void signup_patientWithoutPatientProfile_throwsException() {
        SignupRequest request = mock(SignupRequest.class);
        when(request.role()).thenReturn(UserRole.PATIENT);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void signup_patientWithDoctorProfile_throwsException() {
        SignupRequest request = patientSignupRequest();
        when(request.doctor()).thenReturn(mock(DoctorSignupRequest.class));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void signup_doctorWithoutDoctorProfile_throwsException() {
        SignupRequest request = mock(SignupRequest.class);
        when(request.role()).thenReturn(UserRole.DOCTOR);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void signup_doctorWithPatientProfile_throwsException() {
        SignupRequest request = doctorSignupRequest();
        when(request.patient()).thenReturn(mock(PatientSignupRequest.class));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void signup_doctorWithUnknownHospital_throwsException() {
        SignupRequest request = doctorSignupRequest();
        when(hospitalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_doctorWithDuplicatedLicense_throwsException() {
        SignupRequest request = doctorSignupRequest();
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(mock(Hospital.class)));
        when(doctorRepository.existsByLicenseNumber("123456")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_patientWithDuplicatedProfile_throwsException() {
        SignupRequest request = patientSignupRequest();
        User savedUser = mock(User.class);
        when(savedUser.getId()).thenReturn(1L);
        when(passwordEncoder.encode("password123!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(patientRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void signup_whenPatientSaveFails_propagatesExceptionForRollback() {
        SignupRequest request = patientSignupRequest();
        User savedUser = mock(User.class);
        when(passwordEncoder.encode("password123!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(patientRepository.save(any(Patient.class)))
                .thenThrow(new IllegalStateException("프로필 저장 실패"));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalStateException.class);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_withUnregisteredEmail_throwsInvalidCredentialsException() {
        LoginRequest request = mock(LoginRequest.class);

        when(request.email()).thenReturn("unknown@example.com");
        when(request.password()).thenReturn("password123!");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new InternalAuthenticationServiceException("사용자를 찾을 수 없습니다."));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_success_issuesTokensAndSavesRefreshToken() {
        LoginRequest request = new LoginRequest("patient@example.com", "password123!");
        Authentication authentication = mock(Authentication.class);
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        JwtToken token = new JwtToken("Bearer", "access-token", "refresh-token");
        Date expiration = futureDate();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("patient@example.com");
        when(jwtGenerator.createToken(authentication)).thenReturn(token);
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(user));
        when(jwtProvider.getExpiration("refresh-token")).thenReturn(expiration);
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        JwtToken response = authService.login(request);

        assertThat(response).isEqualTo(token);
        verify(jwtGenerator).createToken(authentication);
        verify(refreshTokenRepository).save(org.mockito.ArgumentMatchers.argThat(refreshToken ->
                refreshToken.getUser() == user
                        && refreshToken.getToken().equals("refresh-token")
                        && refreshToken.getExpiresAt().equals(toLocalDateTime(expiration))
        ));
    }

    @Test
    void login_success_renewsExistingRefreshToken() {
        LoginRequest request = new LoginRequest("patient@example.com", "password123!");
        Authentication authentication = mock(Authentication.class);
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        RefreshToken storedToken = RefreshToken.create(user, "old-refresh-token", LocalDateTime.now().plusDays(1));
        JwtToken token = new JwtToken("Bearer", "access-token", "new-refresh-token");
        Date expiration = futureDate();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("patient@example.com");
        when(jwtGenerator.createToken(authentication)).thenReturn(token);
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(user));
        when(jwtProvider.getExpiration("new-refresh-token")).thenReturn(expiration);
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(storedToken));

        authService.login(request);

        assertThat(storedToken.getToken()).isEqualTo("new-refresh-token");
        assertThat(storedToken.getExpiresAt()).isEqualTo(toLocalDateTime(expiration));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("patient@example.com", "wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("비밀번호 불일치"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(jwtGenerator);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void reissue_withValidRefreshToken_issuesAndRenewsTokens() {
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        RefreshToken storedToken = RefreshToken.create(user, "valid-refresh-token", LocalDateTime.now().plusDays(1));
        JwtToken issuedToken = new JwtToken("Bearer", "new-access-token", "new-refresh-token");
        Date expiration = futureDate();

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(storedToken));
        when(jwtProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtGenerator.createToken(any(Authentication.class))).thenReturn(issuedToken);
        when(jwtProvider.getExpiration("new-refresh-token")).thenReturn(expiration);

        JwtToken response = authService.reissue(new ReissueRequest("valid-refresh-token"));

        assertThat(response).isEqualTo(issuedToken);
        assertThat(storedToken.getToken()).isEqualTo("new-refresh-token");
        assertThat(storedToken.getExpiresAt()).isEqualTo(toLocalDateTime(expiration));
        verify(jwtGenerator).createToken(org.mockito.ArgumentMatchers.argThat(authentication ->
                authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_PATIENT"))
        ));
    }

    @Test
    void reissue_withUnknownRefreshToken_throwsInvalidCredentialsException() {
        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.reissue(new ReissueRequest("unknown-token")))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(jwtGenerator);
    }

    @Test
    void reissue_withInvalidJwt_deletesStoredTokenAndFails() {
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        RefreshToken storedToken = RefreshToken.create(user, "invalid-token", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.of(storedToken));
        when(jwtProvider.validateToken("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.reissue(new ReissueRequest("invalid-token")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository).delete(storedToken);
        verifyNoInteractions(jwtGenerator);
    }

    @Test
    void reissue_withExpiredStoredToken_deletesStoredTokenAndFails() {
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        RefreshToken storedToken = RefreshToken.create(user, "expired-token", LocalDateTime.now().minusSeconds(1));
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(storedToken));
        when(jwtProvider.validateToken("expired-token")).thenReturn(true);

        assertThatThrownBy(() -> authService.reissue(new ReissueRequest("expired-token")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository).delete(storedToken);
    }

    @Test
    void logout_deletesStoredRefreshToken() {
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        ReflectionTestUtils.setField(user, "id", 1L);
        RefreshToken storedToken = RefreshToken.create(user, "refresh-token", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(storedToken));

        authService.logout(1L, new LogoutRequest("refresh-token"));

        verify(refreshTokenRepository).delete(storedToken);
    }

    @Test
    void logout_withAnotherUsersRefreshToken_throwsForbidden() {
        User tokenOwner = User.create("owner@example.com", "encoded-password", UserRole.PATIENT);
        ReflectionTestUtils.setField(tokenOwner, "id", 2L);
        RefreshToken storedToken = RefreshToken.create(
                tokenOwner, "another-users-token", LocalDateTime.now().plusDays(1)
        );
        when(refreshTokenRepository.findByToken("another-users-token")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.logout(1L, new LogoutRequest("another-users-token")))
                .isInstanceOf(AuthForbiddenException.class)
                .extracting("errorCode")
                .isEqualTo(com.medflow.common.exception.ErrorCode.AUTH_FORBIDDEN);

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void logout_withUnknownRefreshToken_keepsExistingPolicy() {
        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        authService.logout(1L, new LogoutRequest("unknown-token"));

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void reissue_afterLogout_failsBecauseRefreshTokenWasDeleted() {
        LogoutRequest logoutRequest = new LogoutRequest("refresh-token");
        ReissueRequest reissueRequest = new ReissueRequest("refresh-token");
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        ReflectionTestUtils.setField(user, "id", 1L);
        RefreshToken storedToken = RefreshToken.create(user, "refresh-token", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("refresh-token"))
                .thenReturn(Optional.of(storedToken))
                .thenReturn(Optional.empty());

        authService.logout(1L, logoutRequest);

        assertThatThrownBy(() -> authService.reissue(reissueRequest))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(refreshTokenRepository).delete(storedToken);
    }

    @Test
    void withdraw_withMatchingPassword_withdrawsUser() {
        Long userId = 1L;
        String rawPassword = "password123!";
        String encodedPassword = "encoded-password";
        User user = User.create("patient@example.com", encodedPassword, UserRole.PATIENT);
        Patient patient = mock(Patient.class);
        WithdrawRequest request = mock(WithdrawRequest.class);

        when(request.password()).thenReturn(rawPassword);
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

        when(request.password()).thenReturn(rawPassword);
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

        when(request.password()).thenReturn(rawPassword);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.empty());

        authService.withdraw(userId, request);

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        verify(refreshTokenRepository).deleteByUserId(userId);
    }

    private SignupRequest patientSignupRequest() {
        SignupRequest request = mock(SignupRequest.class);
        PatientSignupRequest patientRequest = mock(PatientSignupRequest.class);

        when(request.email()).thenReturn("patient@example.com");
        when(request.password()).thenReturn("password123!");
        when(request.role()).thenReturn(UserRole.PATIENT);
        when(request.patient()).thenReturn(patientRequest);
        when(patientRequest.name()).thenReturn("홍길동");
        when(patientRequest.birth()).thenReturn(java.time.LocalDate.of(1999, 5, 20));
        when(patientRequest.gender()).thenReturn(com.medflow.patient.entity.Gender.MALE);
        when(patientRequest.phone()).thenReturn("010-1234-5678");

        return request;
    }

    private SignupRequest doctorSignupRequest() {
        SignupRequest request = mock(SignupRequest.class);
        DoctorSignupRequest doctorRequest = mock(DoctorSignupRequest.class);

        when(request.email()).thenReturn("doctor@example.com");
        when(request.password()).thenReturn("password123!");
        when(request.role()).thenReturn(UserRole.DOCTOR);
        when(request.doctor()).thenReturn(doctorRequest);
        when(doctorRequest.hospitalId()).thenReturn(1L);
        when(doctorRequest.name()).thenReturn("김의사");
        when(doctorRequest.licenseNumber()).thenReturn("123456");
        when(doctorRequest.specialty()).thenReturn("INTERNAL_MEDICINE");
        when(doctorRequest.introduction()).thenReturn("내과 진료를 담당합니다.");
        when(doctorRequest.contact()).thenReturn("02-1234-5678");

        return request;
    }

    private Date futureDate() {
        return Date.from(LocalDateTime.now().plusDays(14)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
