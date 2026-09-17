package com.medflow.doctor.controller;

import com.medflow.doctor.dto.request.DoctorScheduleCreateRequest;
import com.medflow.doctor.dto.response.DoctorScheduleResponse;
import com.medflow.doctor.service.DoctorService;
import com.medflow.security.principal.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoctorControllerTest {

    @Test
    void createDoctorSchedules_returnsCreated() {
        DoctorService doctorService = mock(DoctorService.class);
        UserPrincipal userPrincipal = mock(UserPrincipal.class);
        DoctorController controller = new DoctorController(doctorService);
        DoctorScheduleCreateRequest request = new DoctorScheduleCreateRequest(
                LocalDate.of(2026, 9, 18),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                30
        );
        List<DoctorScheduleResponse> schedules = List.of(
                new DoctorScheduleResponse(
                        10L,
                        LocalDate.of(2026, 9, 18),
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 30)
                )
        );

        when(userPrincipal.getUserId()).thenReturn(1L);
        when(doctorService.createDoctorSchedules(1L, request)).thenReturn(schedules);

        var response = controller.createDoctorSchedules(userPrincipal, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(schedules);
    }
}
