package com.medflow.hospital.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.common.exception.HospitalAlreadyExistsException;
import com.medflow.hospital.dto.request.AdminHospitalCreateRequest;
import com.medflow.hospital.dto.request.AdminHospitalUpdateRequest;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import com.medflow.hospital.repository.HospitalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminHospitalServiceTest {

    @Mock HospitalRepository hospitalRepository;
    @InjectMocks AdminHospitalService adminHospitalService;

    @Test
    void createHospital_savesActiveHospital() {
        AdminHospitalCreateRequest request = createRequest("메드플로우 병원");
        when(hospitalRepository.existsByName("메드플로우 병원")).thenReturn(false);
        when(hospitalRepository.save(any(Hospital.class))).thenAnswer(invocation -> {
            Hospital hospital = invocation.getArgument(0);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            return hospital;
        });

        var response = adminHospitalService.createHospital(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("메드플로우 병원");
        ArgumentCaptor<Hospital> captor = ArgumentCaptor.forClass(Hospital.class);
        verify(hospitalRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(HospitalStatus.ACTIVE);
    }

    @Test
    void createHospital_rejectsDuplicatedName() {
        AdminHospitalCreateRequest request = createRequest("중복 병원");
        when(hospitalRepository.existsByName("중복 병원")).thenReturn(true);

        assertThatThrownBy(() -> adminHospitalService.createHospital(request))
                .isInstanceOf(HospitalAlreadyExistsException.class);

        verify(hospitalRepository, never()).save(any());
    }

    @Test
    void getHospitals_returnsAllHospitalsForAdmin() {
        Hospital active = hospital(1L, "운영 병원", HospitalStatus.ACTIVE);
        Hospital closed = hospital(2L, "폐업 병원", HospitalStatus.CLOSED);
        when(hospitalRepository.findAll()).thenReturn(List.of(active, closed));

        var responses = adminHospitalService.getHospitals();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(response -> response.getStatus())
                .containsExactly(HospitalStatus.ACTIVE, HospitalStatus.CLOSED);
    }

    @Test
    void updateHospital_updatesExistingHospital() {
        Hospital hospital = hospital(1L, "기존 병원", HospitalStatus.ACTIVE);
        AdminHospitalUpdateRequest request = updateRequest("변경 병원", HospitalStatus.ACTIVE);
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.existsByName("변경 병원")).thenReturn(false);

        var response = adminHospitalService.updateHospital(1L, request);

        assertThat(response.getName()).isEqualTo("변경 병원");
        assertThat(response.getAddress()).isEqualTo("서울시 서초구");
        assertThat(response.getStatus()).isEqualTo(HospitalStatus.ACTIVE);
    }

    @Test
    void updateHospital_rejectsDuplicatedChangedName() {
        Hospital hospital = hospital(1L, "기존 병원", HospitalStatus.ACTIVE);
        AdminHospitalUpdateRequest request = updateRequest("중복 병원", HospitalStatus.ACTIVE);
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(hospitalRepository.existsByName("중복 병원")).thenReturn(true);

        assertError(ErrorCode.HOSPITAL_ALREADY_EXISTS,
                () -> adminHospitalService.updateHospital(1L, request));
    }

    @Test
    void updateHospital_failsWhenHospitalDoesNotExist() {
        when(hospitalRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(ErrorCode.HOSPITAL_NOT_FOUND,
                () -> adminHospitalService.updateHospital(999L, updateRequest("병원", HospitalStatus.ACTIVE)));
    }

    @Test
    void deleteHospital_closesAndSoftDeletesHospital() {
        Hospital hospital = hospital(1L, "삭제 병원", HospitalStatus.ACTIVE);
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));

        var response = adminHospitalService.deleteHospital(1L);

        assertThat(hospital.getStatus()).isEqualTo(HospitalStatus.CLOSED);
        assertThat(hospital.isDeleted()).isTrue();
        assertThat(response.getHospitalId()).isEqualTo(1L);
        assertThat(response.getDeleteAt()).isNotNull();
    }

    @Test
    void deleteHospital_failsWhenHospitalDoesNotExist() {
        when(hospitalRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(ErrorCode.HOSPITAL_NOT_FOUND,
                () -> adminHospitalService.deleteHospital(999L));
    }

    private Hospital hospital(Long id, String name, HospitalStatus status) {
        Hospital hospital = Hospital.create(name, "서울시 강남구", "서울", "02-1234-5678", status);
        ReflectionTestUtils.setField(hospital, "id", id);
        return hospital;
    }

    private AdminHospitalCreateRequest createRequest(String name) {
        AdminHospitalCreateRequest request = new AdminHospitalCreateRequest();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "address", "서울시 강남구");
        ReflectionTestUtils.setField(request, "region", "서울");
        ReflectionTestUtils.setField(request, "tel", "02-1234-5678");
        return request;
    }

    private AdminHospitalUpdateRequest updateRequest(String name, HospitalStatus status) {
        AdminHospitalUpdateRequest request = new AdminHospitalUpdateRequest();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "address", "서울시 서초구");
        ReflectionTestUtils.setField(request, "region", "서울");
        ReflectionTestUtils.setField(request, "tel", "02-9876-5432");
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }

    private void assertError(ErrorCode errorCode, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
