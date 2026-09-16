package com.medflow.auth.service;

import com.medflow.auth.dto.request.LoginRequest;
import com.medflow.auth.dto.request.LogoutRequest;
import com.medflow.auth.dto.request.ReissueRequest;
import com.medflow.registration.dto.request.SignupRequest;
import com.medflow.auth.dto.response.TokenResponse;
import com.medflow.registration.dto.response.SignupResponse;
import com.medflow.registration.dto.request.PatientSignupRequest;
import com.medflow.registration.dto.request.DoctorSignupRequest;
import com.medflow.security.jwt.JwtTokenGenerator;
import com.medflow.security.jwt.JwtTokenParser;
import com.medflow.common.exception.InvalidPasswordException;
import com.medflow.common.exception.InvalidCredentialsException;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.EmailAlreadyExistsException;
import com.medflow.common.exception.AuthForbiddenException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.auth.token.repository.RefreshTokenRepository;
import com.medflow.auth.token.entity.RefreshToken;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import com.medflow.user.repository.UserRepository;
import com.medflow.registration.service.RegistrationService;
import com.medflow.user.dto.request.WithdrawRequest;
import com.medflow.user.service.UserAccountService;
import com.medflow.auth.token.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
class AuthenticationFlowTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenGenerator jwtGenerator;

    @Mock
    private JwtTokenParser jwtProvider;

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

    private RegistrationService registrationService;
    private AuthService authenticationService;
    private RefreshTokenService refreshTokenService;
    private UserAccountService userAccountService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
                userRepository, passwordEncoder, patientRepository, doctorRepository, hospitalRepository
        );
        refreshTokenService = new RefreshTokenService(
                userRepository, refreshTokenRepository, jwtGenerator, jwtProvider
        );
        authenticationService = new AuthService(authenticationManager, refreshTokenService);
        userAccountService = new UserAccountService(
                userRepository, passwordEncoder, patientRepository, doctorRepository, refreshTokenService
        );
    }

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

        SignupResponse response = registrationService.signup(request);

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

        SignupResponse response = registrationService.signup(request);

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

        assertThatThrownBy(() -> registrationService.signup(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void signup_withAdminRole_throwsException() {
        SignupRequest request = mock(SignupRequest.class);
        when(request.role()).thenReturn(UserRole.ADMIN);

        assertThatThrownBy(() -> registrationService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(com.medflow.common.exception.ErrorCode.INVALID_SIGNUP_ROLE);
    }

    @Test
    void signup_patientWithoutPatientProfile_throwsException() {
        SignupRequest request = mock(SignupRequest.class);
        when(request.role()).thenReturn(UserRole.PATIENT);

        assertThatThrownBy(() -> registrationService.signup(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void signup_patientWithDoctorProfile_throwsException() {
        SignupRequest request = patientSignupRequest();
        when(request.doctor()).thenReturn(mock(DoctorSignupRequest.class));

        assertThatThrownBy(() -> registrationService.signup(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void signup_doctorWithoutDoctorProfile_throwsException() {
        SignupRequest request = mock(SignupRequest.class);
        when(request.role()).thenReturn(UserRole.DOCTOR);

        assertThatThrownBy(() -> registrationService.signup(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void signup_doctorWithPatientProfile_throwsException() {
        SignupRequest request = doctorSignupRequest();
        when(request.patient()).thenReturn(mock(PatientSignupRequest.class));

        assertThatThrownBy(() -> registrationService.signup(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void signup_doctorWithUnknownHospital_throwsException() {
        SignupRequest request = doctorSignupRequest();
        when(hospitalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.signup(request))
                .isInstanceOf(BusinessException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_doctorWithDuplicatedLicense_throwsException() {
        SignupRequest request = doctorSignupRequest();
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(mock(Hospital.class)));
        when(doctorRepository.existsByLicenseNumber("123456")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.signup(request))
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

        assertThatThrownBy(() -> registrationService.signup(request))
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

        assertThatThrownBy(() -> registrationService.signup(request))
                .isInstanceOf(IllegalStateException.class);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_withUnregisteredEmail_throwsInvalidCredentials() {
        LoginRequest request = mock(LoginRequest.class);

        when(request.email()).thenReturn("unknown@example.com");
        when(request.password()).thenReturn("password123!");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new InternalAuthenticationServiceException("사용자를 찾을 수 없습니다."));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage())
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
                    assertThat(businessException.getErrorCode().getStatus().value()).isEqualTo(401);
                });
    }

    @Test
    void login_success_issuesTokensAndSavesRefreshToken() {
        LoginRequest request = new LoginRequest("patient@example.com", "password123!");
        Authentication authentication = mock(Authentication.class);
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        TokenResponse token = new TokenResponse("Bearer", "access-token", "refresh-token");
        Date expiration = futureDate();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("patient@example.com");
        when(jwtGenerator.createToken(authentication)).thenReturn(token);
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(user));
        when(jwtProvider.getExpiration("refresh-token")).thenReturn(expiration);
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        TokenResponse response = authenticationService.login(request);

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
        TokenResponse token = new TokenResponse("Bearer", "access-token", "new-refresh-token");
        Date expiration = futureDate();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("patient@example.com");
        when(jwtGenerator.createToken(authentication)).thenReturn(token);
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(user));
        when(jwtProvider.getExpiration("new-refresh-token")).thenReturn(expiration);
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(storedToken));

        authenticationService.login(request);

        assertThat(storedToken.getToken()).isEqualTo("new-refresh-token");
        assertThat(storedToken.getExpiresAt()).isEqualTo(toLocalDateTime(expiration));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentials() {
        LoginRequest request = new LoginRequest("patient@example.com", "wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("비밀번호 불일치"));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage())
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        verifyNoInteractions(jwtGenerator);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_withWithdrawnUser_throwsInvalidCredentials() {
        LoginRequest request = new LoginRequest("withdrawn@example.com", "password123!");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("탈퇴한 사용자"));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage())
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
                    assertThat(businessException.getErrorCode().getStatus().value()).isEqualTo(401);
                });

        verifyNoInteractions(jwtGenerator);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void reissue_withValidRefreshToken_issuesAndRenewsTokens() {
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        RefreshToken storedToken = RefreshToken.create(user, "valid-refresh-token", LocalDateTime.now().plusDays(1));
        TokenResponse issuedToken = new TokenResponse("Bearer", "new-access-token", "new-refresh-token");
        Date expiration = futureDate();

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(storedToken));
        when(jwtProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtGenerator.createToken(any(Authentication.class))).thenReturn(issuedToken);
        when(jwtProvider.getExpiration("new-refresh-token")).thenReturn(expiration);

        TokenResponse response = authenticationService.reissue(new ReissueRequest("valid-refresh-token"));

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

        assertThatThrownBy(() -> authenticationService.reissue(new ReissueRequest("unknown-token")))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(jwtGenerator);
    }

    @Test
    void reissue_withInvalidJwt_deletesStoredTokenAndFails() {
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        RefreshToken storedToken = RefreshToken.create(user, "invalid-token", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.of(storedToken));
        when(jwtProvider.validateToken("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.reissue(new ReissueRequest("invalid-token")))
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

        assertThatThrownBy(() -> authenticationService.reissue(new ReissueRequest("expired-token")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository).delete(storedToken);
    }

    @Test
    void logout_deletesStoredRefreshToken() {
        User user = User.create("patient@example.com", "encoded-password", UserRole.PATIENT);
        ReflectionTestUtils.setField(user, "id", 1L);
        RefreshToken storedToken = RefreshToken.create(user, "refresh-token", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(storedToken));

        authenticationService.logout(1L, new LogoutRequest("refresh-token"));

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

        assertThatThrownBy(() -> authenticationService.logout(1L, new LogoutRequest("another-users-token")))
                .isInstanceOf(AuthForbiddenException.class)
                .extracting("errorCode")
                .isEqualTo(com.medflow.common.exception.ErrorCode.AUTH_FORBIDDEN);

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void logout_withUnknownRefreshToken_keepsExistingPolicy() {
        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        authenticationService.logout(1L, new LogoutRequest("unknown-token"));

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

        authenticationService.logout(1L, logoutRequest);

        assertThatThrownBy(() -> authenticationService.reissue(reissueRequest))
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

        userAccountService.withdraw(userId, request);

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

        assertThatThrownBy(() -> userAccountService.withdraw(userId, request))
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

        userAccountService.withdraw(userId, request);

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
