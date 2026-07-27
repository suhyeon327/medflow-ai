package com.medflow.reservation.service;


import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.reservation.dto.request.ReservationCreateRequest;
import com.medflow.reservation.dto.response.PatientReservationResponse;
import com.medflow.reservation.dto.response.ReservationCancelResponse;
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
import java.util.List;
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

    // ----- 예약 생성 기능 ------
    // 예약 성공 테스트
    @Test
    void createReservation_success() {

        // given - 준비
        Long userId = 1L;
        Long patientId = 1L;
        Long scheduleId = 10L;

        Patient patient = createPatient(patientId);

        DoctorSchedule schedule = createAvailableSchedule(scheduleId);

        when(patientRepository.findByUserId(userId))
                .thenReturn(Optional.of(patient));

        when(doctorScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.of(schedule));

        ReservationCreateRequest request =
                new ReservationCreateRequest(scheduleId);

        // when - 실행
        ReservationCreateResponse response =
                reservationService.createReservation(
                        userId,
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
        Long userId = 1L;
        Long scheduleId = 10L;

        DoctorSchedule schedule = createReservedSchedule(scheduleId);

        when(doctorScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.of(schedule));

        ReservationCreateRequest request =
                new ReservationCreateRequest(scheduleId);

        // when & then
        assertThatThrownBy(() ->
                reservationService.createReservation(
                        userId,
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
        Long userId = 1L;
        Long scheduleId = 999L;

        when(doctorScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.empty());

        ReservationCreateRequest request =
                new ReservationCreateRequest(scheduleId);

        // when & then
        assertThatThrownBy(() ->
                reservationService.createReservation(
                        userId,
                        request
                )
        )
                .isInstanceOf(BusinessException.class);
    }

    // 존재하지 않는 환자
    @Test
    void createReservation_fail_when_patient_not_found() {

        // given
        Long userId = 999L;
        Long scheduleId = 10L;

        DoctorSchedule schedule =
                createAvailableSchedule(scheduleId);

        when(doctorScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.of(schedule));

        when(patientRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        ReservationCreateRequest request =
                new ReservationCreateRequest(scheduleId);

        // when & then
        assertThatThrownBy(() ->
                reservationService.createReservation(
                        userId,
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

        Doctor doctor = mock(Doctor.class);

        Hospital hospital = mock(Hospital.class);

        when(doctor.getHospital())
                .thenReturn(hospital);

        when(hospital.getName())
                .thenReturn("테스트 병원");

        when(doctor.getName())
                .thenReturn("테스트 의사");

        DoctorSchedule schedule = DoctorSchedule.create(
                doctor,
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

    // ----- 환자 예약 내역 조회 -----
    // 정상 조회
    @Test
    void getPatientReservations_success() {

        // given
        Long userId = 1L;
        Long patientId = 1L;

        Patient patient = createPatient(patientId);

        Reservation reservation = createReservation(patient);

        when(patientRepository.findByUserId(userId))
                .thenReturn(Optional.of(patient));

        when(reservationRepository.findByPatientId(patientId))
                .thenReturn(List.of(reservation));

        // when
        List<PatientReservationResponse> result =
                reservationService.getPatientReservations(userId);

        // then
        assertThat(result).hasSize(1);

        verify(patientRepository).findByUserId(userId);
        verify(reservationRepository).findByPatientId(patientId);
    }

    // 예약이 없는 경우
    @Test
    void getPatientReservations_empty() {

        // given
        Long userId = 1L;
        Long patientId = 1L;

        Patient patient = createPatient(patientId);

        when(patientRepository.findByUserId(userId))
                .thenReturn(Optional.of(patient));

        when(reservationRepository.findByPatientId(patientId))
                .thenReturn(List.of());

        // when
        List<PatientReservationResponse> result =
                reservationService.getPatientReservations(userId);

        // then
        assertThat(result).isEmpty();

        verify(patientRepository).findByUserId(userId);
        verify(reservationRepository).findByPatientId(patientId);
    }

    // 환자를 찾을 수 없는 경우
    @Test
    void getPatientReservations_fail_when_patient_not_found() {

        // given
        Long userId = 999L;

        when(patientRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                reservationService.getPatientReservations(userId)
        ).isInstanceOf(BusinessException.class);

        verify(reservationRepository, never())
                .findByPatientId(any());
    }

    // ----- 테스트용 Reservation 생성 -----
    private Reservation createReservation(Patient patient) {

        DoctorSchedule schedule = createAvailableSchedule(1L);

        Reservation reservation =
                Reservation.create(patient, schedule);

        ReflectionTestUtils.setField(
                reservation,
                "id",
                1L
        );

        return reservation;
    }

    // ----- 환자 예약 취소 -----
    // 예약 취소 성공
    @Test
    void cancelReservation_success() {

        // given
        Long userId = 1L;
        Long patientId = 1L;
        Long reservationId = 1L;

        Patient patient = createPatient(patientId);
        DoctorSchedule schedule = createAvailableSchedule(10L);
        schedule.reserve();

        Reservation reservation = createReservation(patient, schedule, reservationId);

        when(patientRepository.findByUserId(userId))
                .thenReturn(Optional.of(patient));

        when(reservationRepository.findByIdAndPatientId(reservationId, patientId))
                .thenReturn(Optional.of(reservation));

        // when
        ReservationCancelResponse response =
                reservationService.cancelReservation(userId, reservationId);

        // then
        assertThat(response.reservationId()).isEqualTo(reservationId);
        assertThat(response.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(schedule.getStatus()).isEqualTo(DoctorScheduleStatus.AVAILABLE);
    }

    // 존재하지 않는 예약
    @Test
    void cancelReservation_fail_when_reservation_not_found() {

        // given
        Long userId = 1L;
        Long patientId = 1L;
        Long reservationId = 999L;

        Patient patient = createPatient(patientId);

        when(patientRepository.findByUserId(userId))
                .thenReturn(Optional.of(patient));

        when(reservationRepository.findByIdAndPatientId(reservationId, patientId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                reservationService.cancelReservation(userId, reservationId)
        ).isInstanceOf(BusinessException.class);
    }

    // 존재하지 않는 환자
    @Test
    void cancelReservation_fail_when_patient_not_found() {

        // given
        Long userId = 999L;
        Long reservationId = 1L;

        when(patientRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                reservationService.cancelReservation(userId, reservationId)
        ).isInstanceOf(BusinessException.class);

        verify(reservationRepository, never())
                .findByIdAndPatientId(any(), any());
    }

    // 이미 진료 완료된 예약
    @Test
    void cancelReservation_fail_when_already_completed() {

        // given
        Long userId = 1L;
        Long patientId = 1L;
        Long reservationId = 1L;

        Patient patient = createPatient(patientId);
        DoctorSchedule schedule = createAvailableSchedule(10L);

        Reservation reservation = createReservation(patient, schedule, reservationId);
        reservation.changeStatus(ReservationStatus.COMPLETED);

        when(patientRepository.findByUserId(userId))
                .thenReturn(Optional.of(patient));

        when(reservationRepository.findByIdAndPatientId(reservationId, patientId))
                .thenReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() ->
                reservationService.cancelReservation(userId, reservationId)
        ).isInstanceOf(BusinessException.class);
    }

    // 이미 취소된 예약
    @Test
    void cancelReservation_fail_when_already_cancelled() {

        // given
        Long userId = 1L;
        Long patientId = 1L;
        Long reservationId = 1L;

        Patient patient = createPatient(patientId);
        DoctorSchedule schedule = createAvailableSchedule(10L);

        Reservation reservation = createReservation(patient, schedule, reservationId);
        reservation.cancel();
        schedule.release();

        when(patientRepository.findByUserId(userId))
                .thenReturn(Optional.of(patient));

        when(reservationRepository.findByIdAndPatientId(reservationId, patientId))
                .thenReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() ->
                reservationService.cancelReservation(userId, reservationId)
        ).isInstanceOf(BusinessException.class);
    }

    private Reservation createReservation(Patient patient, DoctorSchedule schedule, Long reservationId) {

        Reservation reservation = Reservation.create(patient, schedule);

        ReflectionTestUtils.setField(reservation, "id", reservationId);

        return reservation;
    }
}