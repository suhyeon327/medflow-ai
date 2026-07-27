package com.medflow.reservation.service;


import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.reservation.dto.request.ReservationCreateRequest;
import com.medflow.reservation.dto.response.ReservationCreateResponse;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.repository.ReservationRepository;

import com.medflow.common.exception.BusinessException;

import com.medflow.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private ReservationService reservationService;

    // 예약 성공 테스트
    @Test
    void createReservation_success() {

        // given - 준비
        Long patientId = 1L;
        Long scheduleId = 10L;

        Patient patient = createPatient(patientId);

        DoctorSchedule schedule = createAvailableSchedule(scheduleId);

        when(patientRepository.findById(patientId))
                .thenReturn(Optional.of(patient));

        when(doctorScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.of(schedule));

        ReservationCreateRequest request =
                new ReservationCreateRequest(scheduleId);

        // when - 실행
        ReservationCreateResponse response =
                reservationService.createReservation(
                        patientId,
                        request
                );

        // then - 검증
        assertThat(response.status())
                .isEqualTo(ReservationStatus.CONFIRMED);

        assertThat(schedule.getStatus())
                .isEqualTo(DoctorScheduleStatus.RESERVED);

        verify(reservationRepository)
                .save(any(Reservation.class));
    }

    // 이미 예약된 시간 예약 실패
    @Test
    void createReservation_fail_when_reserved() {

        // given
        Long patientId = 1L;
        Long scheduleId = 10L;

        DoctorSchedule schedule = createReservedSchedule(scheduleId);

        when(doctorScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.of(schedule));

        ReservationCreateRequest request =
                new ReservationCreateRequest(scheduleId);

        // when & then
        assertThatThrownBy(() ->
                reservationService.createReservation(
                        patientId,
                        request
                )
        )
                .isInstanceOf(BusinessException.class);

        verify(reservationRepository, never())
                .save(any());
    }

    // 존재하지 않는 일정
    @Test
    void createReservation_fail_when_schedule_not_found() {

        // given
        Long patientId = 1L;
        Long scheduleId = 999L;

        when(doctorScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.empty());

        ReservationCreateRequest request =
                new ReservationCreateRequest(scheduleId);

        // when & then
        assertThatThrownBy(() ->
                reservationService.createReservation(
                        patientId,
                        request
                )
        )
                .isInstanceOf(BusinessException.class);
    }

    // 존재하지 않는 환자
    @Test
    void createReservation_fail_when_patient_not_found() {

        // given
        Long patientId = 999L;
        Long scheduleId = 10L;

        DoctorSchedule schedule =
                createAvailableSchedule(scheduleId);

        when(doctorScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.of(schedule));

        when(patientRepository.findById(patientId))
                .thenReturn(Optional.empty());

        ReservationCreateRequest request =
                new ReservationCreateRequest(scheduleId);

        // when & then
        assertThatThrownBy(() ->
                reservationService.createReservation(
                        patientId,
                        request
                )
        )
                .isInstanceOf(BusinessException.class);
    }

    // 환자 생성
    private Patient createPatient(Long id) {

        Patient patient = Patient.create(
                mock(User.class),
                "테스트 환자",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                "01012345678"
        );

        ReflectionTestUtils.setField(
                patient,
                "id",
                id
        );

        return patient;
    }


    // 이용 가능한 스케줄 생성
    private DoctorSchedule createAvailableSchedule(Long id) {

        DoctorSchedule schedule = DoctorSchedule.create(
                null,
                LocalDate.of(2026, 7, 28),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );

        ReflectionTestUtils.setField(
                schedule,
                "id",
                id
        );

        return schedule;
    }


    // 예약된 스케줄 생성
    private DoctorSchedule createReservedSchedule(Long id) {

        DoctorSchedule schedule = createAvailableSchedule(id);

        // AVAILABLE -> RESERVED 변경
        schedule.reserve();

        return schedule;
    }
}