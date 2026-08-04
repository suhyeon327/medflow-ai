package com.medflow.doctor.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.entity.Hospital;
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

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

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
}
