package com.medflow.hospital.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import com.medflow.hospital.repository.HospitalRepository;
import com.medflow.user.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HospitalServiceImplTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private HospitalService hospitalService;

    @Test
    void getAvailableHospitals_withKeyword_trimsKeywordAndSearchesActiveHospitals() {
        String keyword = "서울";
        Hospital hospital = Hospital.create(
                "서울 병원", "서울시 강남구", "서울", "02-1234-5678"
        );
        ReflectionTestUtils.setField(hospital, "id", 1L);
        PageRequest pageable = PageRequest.of(0, 20);

        when(hospitalRepository.searchByStatusAndKeyword(HospitalStatus.ACTIVE, keyword, pageable))
                .thenReturn(new PageImpl<>(List.of(hospital), pageable, 1));
        when(doctorRepository.findAllByHospitalIdInAndStatusAndUserStatus(
                List.of(1L), DoctorStatus.ACTIVE, UserStatus.ACTIVE
        )).thenReturn(List.of());

        assertThat(hospitalService.getAvailableHospitals("  서울  ", pageable).content()).hasSize(1);
        verify(hospitalRepository).searchByStatusAndKeyword(HospitalStatus.ACTIVE, keyword, pageable);
    }

    @Test
    void getAvailableHospitals_withBlankKeyword_returnsAllActiveHospitals() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(hospitalService.getAvailableHospitals(" ", pageable).content()).isEmpty();
        verify(hospitalRepository).findAllByStatus(HospitalStatus.ACTIVE, pageable);
    }

    @Test
    void getAvailableHospitals_mapsDoctorCountAndDistinctSpecialtiesByHospital() {
        Hospital firstHospital = hospital(1L, "메드플로우 병원");
        Hospital secondHospital = hospital(2L, "튼튼 병원");
        List<Doctor> doctors = List.of(
                doctor(firstHospital, "내과"),
                doctor(firstHospital, "내과"),
                doctor(firstHospital, "소아과"),
                doctor(firstHospital, " ")
        );
        PageRequest pageable = PageRequest.of(0, 20);
        when(hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(firstHospital, secondHospital), pageable, 2));
        when(doctorRepository.findAllByHospitalIdInAndStatusAndUserStatus(
                List.of(1L, 2L), DoctorStatus.ACTIVE, UserStatus.ACTIVE
        )).thenReturn(doctors);

        var response = hospitalService.getAvailableHospitals(null, pageable);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().getFirst()).satisfies(hospitalResponse -> {
            assertThat(hospitalResponse.id()).isEqualTo(1L);
            assertThat(hospitalResponse.name()).isEqualTo("메드플로우 병원");
            assertThat(hospitalResponse.region()).isEqualTo("서울");
            assertThat(hospitalResponse.doctorCount()).isEqualTo(4);
            assertThat(hospitalResponse.specialties()).containsExactly("내과", "소아과");
        });
        assertThat(response.content().get(1).doctorCount()).isZero();
        assertThat(response.content().get(1).specialties()).isEmpty();
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }

    @Test
    void getAvailableHospitals_whenPageIsEmpty_skipsDoctorLookup() {
        PageRequest pageable = PageRequest.of(3, 20);
        when(hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 40));

        var response = hospitalService.getAvailableHospitals(null, pageable);

        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isEqualTo(3);
        assertThat(response.totalElements()).isEqualTo(40);
        assertThat(response.last()).isTrue();
        verify(doctorRepository, never()).findAllByHospitalIdInAndStatusAndUserStatus(
                anyList(), any(), any()
        );
    }

    @Test
    void getDetailHospital_whenActiveHospitalExists_returnsHospitalDetail() {
        Hospital hospital = hospital(1L, "메드플로우 병원");
        when(hospitalRepository.findByIdAndStatus(1L, HospitalStatus.ACTIVE))
                .thenReturn(Optional.of(hospital));

        var response = hospitalService.getDetailHospital(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("메드플로우 병원");
        assertThat(response.tel()).isEqualTo("02-1234-5678");
    }

    @Test
    void getAvailableDoctors_whenActiveHospitalExists_returnsActiveApprovedDoctors() {
        Long hospitalId = 1L;
        Hospital hospital = hospital(hospitalId, "메드플로우 병원");
        var doctor = doctor(hospital, "내과");
        when(doctor.getId()).thenReturn(10L);
        when(doctor.getName()).thenReturn("김의사");
        when(doctor.getContact()).thenReturn(null);

        when(hospitalRepository.findByIdAndStatus(hospitalId, HospitalStatus.ACTIVE))
                .thenReturn(Optional.of(hospital));
        when(doctorRepository.findAllByHospitalIdAndStatusAndUserStatus(
                hospitalId, DoctorStatus.ACTIVE, UserStatus.ACTIVE
        )).thenReturn(List.of(doctor));

        assertThat(hospitalService.getAvailableDoctors(hospitalId)).singleElement().satisfies(response -> {
            assertThat(response.getDoctorId()).isEqualTo(10L);
            assertThat(response.getDoctorName()).isEqualTo("김의사");
            assertThat(response.getHospitalId()).isEqualTo(hospitalId);
            assertThat(response.getSpecialty()).isEqualTo("내과");
            assertThat(response.getContact()).isEqualTo("02-1234-5678");
        });

        verify(doctorRepository).findAllByHospitalIdAndStatusAndUserStatus(
                hospitalId, DoctorStatus.ACTIVE, UserStatus.ACTIVE
        );
    }

    @Test
    void getAvailableDoctors_whenActiveHospitalDoesNotExist_throwsHospitalNotFound() {
        Long hospitalId = 999L;
        when(hospitalRepository.findByIdAndStatus(hospitalId, HospitalStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> hospitalService.getAvailableDoctors(hospitalId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HOSPITAL_NOT_FOUND);
    }

    @Test
    void getDetailHospital_whenHospitalIsNotActive_throwsHospitalNotFound() {
        Long hospitalId = 1L;
        when(hospitalRepository.findByIdAndStatus(hospitalId, HospitalStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> hospitalService.getDetailHospital(hospitalId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HOSPITAL_NOT_FOUND);
    }

    private Hospital hospital(Long id, String name) {
        Hospital hospital = Hospital.create(name, "서울시 강남구", "서울", "02-1234-5678");
        ReflectionTestUtils.setField(hospital, "id", id);
        return hospital;
    }

    private Doctor doctor(Hospital hospital, String specialty) {
        Doctor doctor = mock(Doctor.class);
        when(doctor.getHospital()).thenReturn(hospital);
        when(doctor.getSpecialty()).thenReturn(specialty);
        return doctor;
    }
}
