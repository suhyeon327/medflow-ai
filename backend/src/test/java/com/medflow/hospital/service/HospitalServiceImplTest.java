package com.medflow.hospital.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
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
    void getAvailableHospitals_searchesNameRegionAndAddressByKeyword() {
        String keyword = "서울";
        Hospital hospital = mock(Hospital.class);

        when(hospitalRepository.searchByStatusAndKeyword(HospitalStatus.ACTIVE, keyword))
                .thenReturn(List.of(hospital));

        assertThat(hospitalService.getAvailableHospitals("  서울  ")).hasSize(1);
        verify(hospitalRepository).searchByStatusAndKeyword(HospitalStatus.ACTIVE, keyword);
    }

    @Test
    void getAvailableHospitals_withoutKeywordReturnsAllActiveHospitals() {
        when(hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE)).thenReturn(List.of());

        assertThat(hospitalService.getAvailableHospitals(" ")).isEmpty();
        verify(hospitalRepository).findAllByStatus(HospitalStatus.ACTIVE);
    }

    @Test
    void getAvailableHospitals_returnsMappedActiveHospitals() {
        Hospital hospital = Hospital.create(
                "메드플로우 병원", "서울시 강남구", "서울", "02-1234-5678", HospitalStatus.ACTIVE
        );
        ReflectionTestUtils.setField(hospital, "id", 1L);
        when(hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE)).thenReturn(List.of(hospital));

        var responses = hospitalService.getAvailableHospitals(null);

        assertThat(responses).singleElement().satisfies(response -> {
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("메드플로우 병원");
            assertThat(response.getRegion()).isEqualTo("서울");
        });
    }

    @Test
    void getDetailHospital_returnsActiveHospital() {
        Hospital hospital = Hospital.create(
                "메드플로우 병원", "서울시 강남구", "서울", "02-1234-5678", HospitalStatus.ACTIVE
        );
        ReflectionTestUtils.setField(hospital, "id", 1L);
        when(hospitalRepository.findByIdAndStatus(1L, HospitalStatus.ACTIVE))
                .thenReturn(Optional.of(hospital));

        var response = hospitalService.getDetailHospital(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("메드플로우 병원");
        assertThat(response.getTel()).isEqualTo("02-1234-5678");
    }

    @Test
    void getAvailableDoctors_returnsOnlyActiveApprovedDoctors() {
        Long hospitalId = 1L;
        Hospital hospital = mock(Hospital.class);

        when(hospitalRepository.findByIdAndStatus(hospitalId, HospitalStatus.ACTIVE))
                .thenReturn(Optional.of(hospital));
        when(doctorRepository.findAllByHospitalIdAndStatusAndUserStatus(
                hospitalId, DoctorStatus.ACTIVE, UserStatus.ACTIVE
        )).thenReturn(List.of());

        assertThat(hospitalService.getAvailableDoctors(hospitalId)).isEmpty();

        verify(doctorRepository).findAllByHospitalIdAndStatusAndUserStatus(
                hospitalId, DoctorStatus.ACTIVE, UserStatus.ACTIVE
        );
    }

    @Test
    void getAvailableDoctors_throwsWhenActiveHospitalDoesNotExist() {
        Long hospitalId = 999L;
        when(hospitalRepository.findByIdAndStatus(hospitalId, HospitalStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> hospitalService.getAvailableDoctors(hospitalId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HOSPITAL_NOT_FOUND);
    }

    @Test
    void getDetailHospital_throwsWhenHospitalIsNotActive() {
        Long hospitalId = 1L;
        when(hospitalRepository.findByIdAndStatus(hospitalId, HospitalStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> hospitalService.getDetailHospital(hospitalId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HOSPITAL_NOT_FOUND);
    }
}
