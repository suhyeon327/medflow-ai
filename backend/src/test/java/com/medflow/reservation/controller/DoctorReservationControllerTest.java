package com.medflow.reservation.controller;

import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.reservation.service.DoctorReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void getDoctorReservationsByDate_returnsBadRequest_whenDateFormatIsInvalid() throws Exception {

        mockMvc.perform(get("/api/v1/doctors/reservations/date").param("date", "2026-07-30-invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchDoctorReservations_returnsBadRequest_whenStatusIsInvalid() throws Exception {

        mockMvc.perform(get("/api/v1/doctors/reservations/search").param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }
}
