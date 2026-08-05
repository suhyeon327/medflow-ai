package com.medflow.doctor.service;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.dto.request.DoctorUpdateRequest;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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

        doctorService.updateDoctorProfile(userId, request);

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
