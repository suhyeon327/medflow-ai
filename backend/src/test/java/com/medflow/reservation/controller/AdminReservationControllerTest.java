package com.medflow.reservation.controller;

import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.reservation.service.AdminReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminReservationControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminReservationController(mock(AdminReservationService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void searchReservations_returnsBadRequest_whenStatusIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reservations").param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchReservations_allowsOnlyAdminRole() {
        PreAuthorize preAuthorize = AdminReservationController.class.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize.value()).isEqualTo("hasRole('ADMIN')");
    }
}
