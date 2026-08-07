package com.medflow.doctor.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.dto.request.DoctorScheduleCreateRequest;
import com.medflow.doctor.dto.request.DoctorUpdateRequest;
import com.medflow.doctor.dto.response.DoctorProfileResponse;
import com.medflow.doctor.dto.response.DoctorScheduleResponse;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import com.medflow.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock DoctorRepository doctorRepository;
    @Mock DoctorScheduleRepository doctorScheduleRepository;
    @Mock HospitalRepository hospitalRepository;
    @InjectMocks DoctorService doctorService;

    @Test
    void getDoctorProfile_returnsOwnProfile() {
        Hospital hospital = hospital(10L, "메드플로우 병원");
        Doctor doctor = doctor(1L, hospital, DoctorStatus.ACTIVE);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));

        DoctorProfileResponse response = doctorService.getDoctorProfile(100L);

        assertThat(response.doctorId()).isEqualTo(1L);
        assertThat(response.doctorName()).isEqualTo("홍길동");
        assertThat(response.licenseNumber()).isEqualTo("LICENSE-001");
        assertThat(response.hospitalId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(DoctorStatus.ACTIVE);
    }

    @Test
    void getDoctorProfile_failsWhenDoctorDoesNotExist() {
        when(doctorRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertError(ErrorCode.DOCTOR_NOT_FOUND, () -> doctorService.getDoctorProfile(999L));
    }

    @Test
    void updateDoctorProfile_updatesPendingDoctorCertificationAndProfile() {
        Hospital oldHospital = hospital(10L, "기존 병원");
        Hospital newHospital = hospital(20L, "변경 병원");
        Doctor doctor = doctor(1L, oldHospital, DoctorStatus.PENDING);
        DoctorUpdateRequest request = updateRequest(20L, "김의사", "LICENSE-002");
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(20L)).thenReturn(Optional.of(newHospital));

        DoctorProfileResponse response = doctorService.updateDoctorProfile(100L, request);

        assertThat(response.doctorName()).isEqualTo("김의사");
        assertThat(response.licenseNumber()).isEqualTo("LICENSE-002");
        assertThat(response.hospitalId()).isEqualTo(20L);
        assertThat(response.specialty()).isEqualTo("내과");
    }

    @Test
    void updateDoctorProfile_allowsActiveDoctorToUpdateNonCertificationFields() {
        Hospital hospital = hospital(10L, "메드플로우 병원");
        Doctor doctor = doctor(1L, hospital, DoctorStatus.ACTIVE);
        DoctorUpdateRequest request = updateRequest(10L, "홍길동", "LICENSE-001");
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(10L)).thenReturn(Optional.of(hospital));

        DoctorProfileResponse response = doctorService.updateDoctorProfile(100L, request);

        assertThat(response.specialty()).isEqualTo("내과");
        assertThat(response.introduction()).isEqualTo("환자 중심 진료");
        assertThat(response.contact()).isEqualTo("02-1234-5678");
    }

    @Test
    void updateDoctorProfile_rejectsCertificationChangeAfterApproval() {
        Hospital oldHospital = hospital(10L, "기존 병원");
        Hospital newHospital = hospital(20L, "변경 병원");
        Doctor doctor = doctor(1L, oldHospital, DoctorStatus.ACTIVE);
        DoctorUpdateRequest request = updateRequest(20L, "김의사", "LICENSE-002");
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(20L)).thenReturn(Optional.of(newHospital));

        assertError(ErrorCode.INVALID_DOCTOR_STATUS,
                () -> doctorService.updateDoctorProfile(100L, request));
    }

    @Test
    void updateDoctorProfile_failsWhenHospitalDoesNotExist() {
        Doctor doctor = doctor(1L, hospital(10L, "기존 병원"), DoctorStatus.PENDING);
        DoctorUpdateRequest request = updateRequest(999L, "홍길동", "LICENSE-001");
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(ErrorCode.HOSPITAL_NOT_FOUND,
                () -> doctorService.updateDoctorProfile(100L, request));
    }

    @Test
    void updateDoctorProfile_rejectsDuplicatedLicenseNumber() {
        Doctor doctor = doctor(1L, hospital(10L, "기존 병원"), DoctorStatus.PENDING);
        DoctorUpdateRequest request = updateRequest(10L, "홍길동", "DUPLICATED");
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(hospitalRepository.findById(10L)).thenReturn(Optional.of(doctor.getHospital()));
        when(doctorRepository.existsByLicenseNumber("DUPLICATED")).thenReturn(true);

        assertError(ErrorCode.LICENSE_NUMBER_ALREADY_EXISTS,
                () -> doctorService.updateDoctorProfile(100L, request));
    }

    @Test
    void createDoctorSchedules_createsSlotsForActiveDoctor() {
        Doctor doctor = doctor(1L, hospital(10L, "병원"), DoctorStatus.ACTIVE);
        DoctorScheduleCreateRequest request = scheduleRequest(LocalTime.of(9, 0), LocalTime.of(10, 0), 30);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.saveAndFlush(any(DoctorSchedule.class))).thenAnswer(invocation -> {
            DoctorSchedule schedule = invocation.getArgument(0);
            ReflectionTestUtils.setField(schedule, "id", schedule.getStartTime().equals(LocalTime.of(9, 0)) ? 1L : 2L);
            return schedule;
        });

        List<DoctorScheduleResponse> responses = doctorService.createDoctorSchedules(100L, request);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(DoctorScheduleResponse::startTime)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 30));
        ArgumentCaptor<DoctorSchedule> captor = ArgumentCaptor.forClass(DoctorSchedule.class);
        verify(doctorScheduleRepository, org.mockito.Mockito.times(2)).saveAndFlush(captor.capture());
        assertThat(captor.getAllValues()).allMatch(schedule -> schedule.getDoctor() == doctor);
    }

    @Test
    void createDoctorSchedules_rejectsDuplicatedStartTime() {
        Doctor doctor = doctor(1L, hospital(10L, "병원"), DoctorStatus.ACTIVE);
        DoctorScheduleCreateRequest request = scheduleRequest(LocalTime.of(9, 0), LocalTime.of(10, 0), 30);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.existsOverlappingSchedule(
                1L, request.date(), request.startTime(), request.endTime())).thenReturn(true);

        assertError(ErrorCode.SCHEDULE_NOT_AVAILABLE,
                () -> doctorService.createDoctorSchedules(100L, request));
        verify(doctorScheduleRepository, never()).saveAndFlush(any());
    }

    @Test
    void createDoctorSchedules_doesNotCreatePartialSlotBeyondEndTime() {
        Doctor doctor = doctor(1L, hospital(10L, "병원"), DoctorStatus.ACTIVE);
        DoctorScheduleCreateRequest request = scheduleRequest(LocalTime.of(9, 0), LocalTime.of(9, 50), 30);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.saveAndFlush(any(DoctorSchedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<DoctorScheduleResponse> responses = doctorService.createDoctorSchedules(100L, request);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().endTime()).isEqualTo(LocalTime.of(9, 30));
    }

    @Test
    void createDoctorSchedules_allowsAdjacentSchedule() {
        Doctor doctor = doctor(1L, hospital(10L, "병원"), DoctorStatus.ACTIVE);
        DoctorScheduleCreateRequest request = scheduleRequest(LocalTime.of(9, 30), LocalTime.of(10, 0), 30);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.existsOverlappingSchedule(
                1L, request.date(), request.startTime(), request.endTime())).thenReturn(false);
        when(doctorScheduleRepository.saveAndFlush(any(DoctorSchedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<DoctorScheduleResponse> responses = doctorService.createDoctorSchedules(100L, request);

        assertThat(responses).singleElement().satisfies(response -> {
            assertThat(response.startTime()).isEqualTo(LocalTime.of(9, 30));
            assertThat(response.endTime()).isEqualTo(LocalTime.of(10, 0));
        });
    }

    @Test
    void createDoctorSchedules_rejectsEqualStartAndEndTime() {
        DoctorScheduleCreateRequest request = scheduleRequest(LocalTime.of(9, 0), LocalTime.of(9, 0), 30);

        assertError(ErrorCode.INVALID_SCHEDULE_TIME_RANGE,
                () -> doctorService.createDoctorSchedules(100L, request));
        verify(doctorRepository, never()).findByUserId(any());
        verify(doctorScheduleRepository, never()).saveAndFlush(any());
    }

    @Test
    void createDoctorSchedules_rejectsEndTimeBeforeStartTime() {
        DoctorScheduleCreateRequest request = scheduleRequest(LocalTime.of(9, 30), LocalTime.of(9, 0), 30);

        assertError(ErrorCode.INVALID_SCHEDULE_TIME_RANGE,
                () -> doctorService.createDoctorSchedules(100L, request));
        verify(doctorRepository, never()).findByUserId(any());
        verify(doctorScheduleRepository, never()).saveAndFlush(any());
    }

    @Test
    void createDoctorSchedules_convertsUniqueConstraintViolation() {
        Doctor doctor = doctor(1L, hospital(10L, "병원"), DoctorStatus.ACTIVE);
        DoctorScheduleCreateRequest request = scheduleRequest(LocalTime.of(9, 0), LocalTime.of(9, 30), 30);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.saveAndFlush(any(DoctorSchedule.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate schedule"));

        assertError(ErrorCode.SCHEDULE_NOT_AVAILABLE,
                () -> doctorService.createDoctorSchedules(100L, request));
    }

    @Test
    void createDoctorSchedules_rejectsPendingDoctor() {
        assertScheduleCreationRejected(DoctorStatus.PENDING);
    }

    @Test
    void createDoctorSchedules_rejectsRejectedDoctor() {
        assertScheduleCreationRejected(DoctorStatus.REJECTED);
    }

    @Test
    void createDoctorSchedules_failsWhenDoctorDoesNotExist() {
        when(doctorRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertError(ErrorCode.DOCTOR_NOT_FOUND, () -> doctorService.createDoctorSchedules(
                999L, scheduleRequest(LocalTime.of(9, 0), LocalTime.of(10, 0), 30)));
        verify(doctorScheduleRepository, never()).save(any());
    }

    @Test
    void getDoctorSchedules_returnsOwnSchedules() {
        Doctor doctor = doctor(1L, hospital(10L, "병원"), DoctorStatus.ACTIVE);
        DoctorSchedule schedule = DoctorSchedule.create(
                doctor, LocalDate.of(2026, 8, 10), LocalTime.of(9, 0), LocalTime.of(9, 30));
        ReflectionTestUtils.setField(schedule, "id", 5L);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorId(1L)).thenReturn(List.of(schedule));

        List<DoctorScheduleResponse> responses = doctorService.getDoctorSchedules(100L, null);

        assertThat(responses).singleElement().satisfies(response -> {
            assertThat(response.scheduleId()).isEqualTo(5L);
            assertThat(response.startTime()).isEqualTo(LocalTime.of(9, 0));
        });
    }

    @Test
    void getDoctorSchedules_filtersOwnSchedulesByDate() {
        Doctor doctor = doctor(1L, hospital(10L, "병원"), DoctorStatus.ACTIVE);
        LocalDate date = LocalDate.of(2026, 8, 10);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorIdAndDate(1L, date)).thenReturn(List.of());

        assertThat(doctorService.getDoctorSchedules(100L, date)).isEmpty();
        verify(doctorScheduleRepository).findByDoctorIdAndDate(1L, date);
    }

    private void assertScheduleCreationRejected(DoctorStatus status) {
        Doctor doctor = doctor(1L, hospital(10L, "병원"), status);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));

        assertError(ErrorCode.DOCTOR_NOT_APPROVED, () -> doctorService.createDoctorSchedules(
                100L, scheduleRequest(LocalTime.of(9, 0), LocalTime.of(10, 0), 30)));
        verify(doctorScheduleRepository, never()).save(any());
    }

    private Doctor doctor(Long id, Hospital hospital, DoctorStatus status) {
        Doctor doctor = Doctor.create(mock(User.class), hospital, "홍길동", "LICENSE-001");
        ReflectionTestUtils.setField(doctor, "id", id);
        ReflectionTestUtils.setField(doctor, "status", status);
        return doctor;
    }

    private Hospital hospital(Long id, String name) {
        Hospital hospital = mock(Hospital.class);
        lenient().when(hospital.getId()).thenReturn(id);
        lenient().when(hospital.getName()).thenReturn(name);
        return hospital;
    }

    private DoctorUpdateRequest updateRequest(Long hospitalId, String name, String licenseNumber) {
        DoctorUpdateRequest request = new DoctorUpdateRequest();
        ReflectionTestUtils.setField(request, "hospitalId", hospitalId);
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "licenseNumber", licenseNumber);
        ReflectionTestUtils.setField(request, "specialty", "내과");
        ReflectionTestUtils.setField(request, "introduction", "환자 중심 진료");
        ReflectionTestUtils.setField(request, "contact", "02-1234-5678");
        return request;
    }

    private DoctorScheduleCreateRequest scheduleRequest(LocalTime start, LocalTime end, int slotMinutes) {
        return new DoctorScheduleCreateRequest(LocalDate.of(2026, 8, 10), start, end, slotMinutes);
    }

    private void assertError(ErrorCode errorCode, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
