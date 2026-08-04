package com.medflow.doctor.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.dto.request.DoctorUpdateRequest;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import com.medflow.user.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void getPublicDoctor_returnsPublicInformation() {
        Long doctorId = 1L;
        Doctor doctor = mock(Doctor.class);
        Hospital hospital = mock(Hospital.class);

        when(doctorRepository.findByIdAndStatusAndUserStatus(
                doctorId, DoctorStatus.ACTIVE, UserStatus.ACTIVE
        )).thenReturn(Optional.of(doctor));
        when(doctor.getId()).thenReturn(doctorId);
        when(doctor.getName()).thenReturn("홍길동");
        when(doctor.getHospital()).thenReturn(hospital);
        when(hospital.getId()).thenReturn(10L);
        when(hospital.getName()).thenReturn("메드플로우 병원");

        var response = doctorService.getPublicDoctor(doctorId);

        assertThat(response.getDoctorId()).isEqualTo(doctorId);
        assertThat(response.getDoctorName()).isEqualTo("홍길동");
        assertThat(response.getHospitalId()).isEqualTo(10L);
    }

    @Test
    void getPublicDoctor_throwsWhenActiveDoctorDoesNotExist() {
        Long doctorId = 999L;
        when(doctorRepository.findByIdAndStatusAndUserStatus(
                doctorId, DoctorStatus.ACTIVE, UserStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getPublicDoctor(doctorId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void update_updatesDoctorProfileInformation() {
        Long userId = 1L;
        Doctor doctor = mock(Doctor.class);
        Hospital hospital = mock(Hospital.class);
        DoctorUpdateRequest request = mock(DoctorUpdateRequest.class);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(request.getHospitalId()).thenReturn(10L);
        when(request.getName()).thenReturn("홍길동");
        when(request.getLicenseNumber()).thenReturn("LICENSE-001");
        when(request.getSpecialty()).thenReturn("내과");
        when(request.getIntroduction()).thenReturn("환자 중심 진료를 제공합니다.");
        when(request.getContact()).thenReturn("02-1234-5678");
        when(hospitalRepository.findById(10L)).thenReturn(Optional.of(hospital));
        when(doctor.getLicenseNumber()).thenReturn("LICENSE-001");
        when(doctor.getHospital()).thenReturn(hospital);

        doctorService.update(userId, request);

        verify(doctor).update(
                hospital,
                "홍길동",
                "LICENSE-001",
                "내과",
                "환자 중심 진료를 제공합니다.",
                "02-1234-5678"
        );
    }
}
