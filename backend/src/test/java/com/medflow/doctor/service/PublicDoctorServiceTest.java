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
class PublicDoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private PublicDoctorService publicDoctorService;

    @Test
    void 공개_의사_상세를_조회한다() {
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

        var response = publicDoctorService.getDoctor(doctorId);

        assertThat(response.doctorId()).isEqualTo(doctorId);
        assertThat(response.doctorName()).isEqualTo("홍길동");
        assertThat(response.hospitalId()).isEqualTo(10L);
    }

    @Test
    void 활성_의사가_없으면_공개_상세_조회에_실패한다() {
        Long doctorId = 999L;
        when(doctorRepository.findByIdAndStatusAndUserStatus(
                doctorId, DoctorStatus.ACTIVE, UserStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicDoctorService.getDoctor(doctorId))
                .isInstanceOf(BusinessException.class);
    }
}
