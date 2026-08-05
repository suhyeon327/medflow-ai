package com.medflow.auth.service;

import com.medflow.auth.dto.request.WithdrawRequest;
import com.medflow.auth.dto.request.LoginRequest;
import com.medflow.auth.dto.request.SignupRequest;
import com.medflow.auth.dto.response.SignupResponse;
import com.medflow.auth.dto.request.PatientSignupRequest;
import com.medflow.auth.dto.request.DoctorSignupRequest;
import com.medflow.auth.jwt.JwtGenerator;
import com.medflow.auth.jwt.JwtProvider;
import com.medflow.common.exception.InvalidPasswordException;
import com.medflow.common.exception.InvalidCredentialsException;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.EmailAlreadyExistsException;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                .isInstanceOf(BusinessException.class);
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
}
