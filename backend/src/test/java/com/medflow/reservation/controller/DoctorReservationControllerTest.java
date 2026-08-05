package com.medflow.reservation.controller;

import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.reservation.service.DoctorReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

class DoctorReservationControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DoctorReservationController(mock(DoctorReservationService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getDoctorReservations_returnsBadRequest_whenDateFormatIsInvalid() throws Exception {

        mockMvc.perform(get("/api/v1/doctors/reservations").param("date", "2026-07-30-invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDoctorReservations_returnsBadRequest_whenStatusIsInvalid() throws Exception {

        mockMvc.perform(get("/api/v1/doctors/reservations").param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReservationStatus_returnsBadRequest_whenStatusIsMissing() throws Exception {

        mockMvc.perform(patch("/api/v1/doctors/reservations/1/status")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void questionnaireAnalysisEndpoint_isRestrictedToDoctorRole() {
        PreAuthorize preAuthorize = DoctorReservationController.class.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('DOCTOR')");
    }
}
