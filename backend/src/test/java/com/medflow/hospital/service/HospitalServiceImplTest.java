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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
        Hospital hospital = Hospital.create(
                "서울 병원", "서울시 강남구", "서울", "02-1234-5678", HospitalStatus.ACTIVE
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
    void getAvailableHospitals_withoutKeywordReturnsAllActiveHospitals() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(hospitalService.getAvailableHospitals(" ", pageable).content()).isEmpty();
        verify(hospitalRepository).findAllByStatus(HospitalStatus.ACTIVE, pageable);
    }

    @Test
    void getAvailableHospitals_returnsMappedActiveHospitals() {
        Hospital hospital = Hospital.create(
                "메드플로우 병원", "서울시 강남구", "서울", "02-1234-5678", HospitalStatus.ACTIVE
        );
        ReflectionTestUtils.setField(hospital, "id", 1L);
        PageRequest pageable = PageRequest.of(0, 20);
        when(hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(hospital), pageable, 1));
        when(doctorRepository.findAllByHospitalIdInAndStatusAndUserStatus(
                List.of(1L), DoctorStatus.ACTIVE, UserStatus.ACTIVE
        )).thenReturn(List.of());

        var response = hospitalService.getAvailableHospitals(null, pageable);

        assertThat(response.content()).singleElement().satisfies(hospitalResponse -> {
            assertThat(hospitalResponse.getId()).isEqualTo(1L);
            assertThat(hospitalResponse.getName()).isEqualTo("메드플로우 병원");
            assertThat(hospitalResponse.getRegion()).isEqualTo("서울");
        });
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }

    @Test
    void getAvailableHospitals_returnsEmptyPage() {
        PageRequest pageable = PageRequest.of(3, 20);
        when(hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 40));

        var response = hospitalService.getAvailableHospitals(null, pageable);

        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isEqualTo(3);
        assertThat(response.totalElements()).isEqualTo(40);
        assertThat(response.last()).isTrue();
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
