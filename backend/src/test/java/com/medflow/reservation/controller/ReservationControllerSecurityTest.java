package com.medflow.reservation.controller;

import com.medflow.auth.jwt.JwtProvider;
import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.config.SecurityConfig;
import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.common.security.CustomAuthenticationEntryPoint;
import com.medflow.reservation.dto.response.AdminReservationPageResponse;
import com.medflow.reservation.dto.response.DoctorReservationPageResponse;
import com.medflow.reservation.dto.response.PatientReservationPageResponse;
import com.medflow.reservation.dto.response.ReservationCancelResponse;
import com.medflow.reservation.dto.response.ReservationCreateResponse;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.service.AdminReservationService;
import com.medflow.reservation.service.DoctorReservationService;
import com.medflow.reservation.service.ReservationService;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        ReservationController.class,
        DoctorReservationController.class,
        AdminReservationController.class
})
@ContextConfiguration(classes = {
        ReservationController.class,
        DoctorReservationController.class,
        AdminReservationController.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        CustomAuthenticationEntryPoint.class
})
class ReservationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private DoctorReservationService doctorReservationService;

    @MockitoBean
    private AdminReservationService adminReservationService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void patientCanCreateReservation() throws Exception {
        // given
        when(reservationService.createReservation(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(new ReservationCreateResponse(100L, ReservationStatus.APPROVED));

        // when & then
        mockMvc.perform(post("/api/v1/reservations/")
                        .with(user(userDetails(1L, UserRole.PATIENT)))
                        .contentType("application/json")
                        .content("{\"scheduleId\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reservationId").value(100L))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void patientCanCancelOwnReservation() throws Exception {
        // given
        when(reservationService.cancelReservation(1L, 100L))
                .thenReturn(new ReservationCancelResponse(100L, ReservationStatus.CANCELLED));

        // when & then
        mockMvc.perform(patch("/api/v1/reservations/{reservationId}/cancel", 100L)
                        .with(user(userDetails(1L, UserRole.PATIENT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reservationId").value(100L))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        verify(reservationService).cancelReservation(1L, 100L);
    }

    @Test
    void patientCanAccessPatientReservationApi() throws Exception {
        // given
        when(reservationService.getPatientReservations(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any()
        ))
                .thenReturn(new PatientReservationPageResponse(List.of(), 0, 10, 0, 0));

        // when & then
        mockMvc.perform(get("/api/v1/reservations/patient").with(user(userDetails(1L, UserRole.PATIENT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(reservationService).getPatientReservations(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void doctorCanAccessDoctorReservationApi() throws Exception {
        // given
        when(doctorReservationService.getDoctorReservations(
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(new DoctorReservationPageResponse(List.of(), 0, 10, 0, 0));

        // when & then
        mockMvc.perform(get("/api/v1/doctors/reservations").with(user(userDetails(2L, UserRole.DOCTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void adminCanAccessAdminReservationApi() throws Exception {
        // given
        when(adminReservationService.searchReservations(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(new AdminReservationPageResponse(List.of(), 0, 20, 0, 0));

        // when & then
        mockMvc.perform(get("/api/v1/admin/reservations").with(user(userDetails(3L, UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @ParameterizedTest
    @MethodSource("forbiddenRoleRequests")
    void reservationApisRejectOtherRoles(String path, UserRole role) throws Exception {
        mockMvc.perform(get(path).with(user(userDetails(10L, role))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @ParameterizedTest
    @MethodSource("reservationApiPaths")
    void reservationApisRequireAuthentication(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    private static Stream<Arguments> forbiddenRoleRequests() {
        return Stream.of(
                Arguments.of("/api/v1/reservations/patient", UserRole.DOCTOR),
                Arguments.of("/api/v1/reservations/patient", UserRole.ADMIN),
                Arguments.of("/api/v1/doctors/reservations", UserRole.PATIENT),
                Arguments.of("/api/v1/doctors/reservations", UserRole.ADMIN),
                Arguments.of("/api/v1/admin/reservations", UserRole.PATIENT),
                Arguments.of("/api/v1/admin/reservations", UserRole.DOCTOR)
        );
    }

    private static Stream<String> reservationApiPaths() {
        return Stream.of(
                "/api/v1/reservations/patient",
                "/api/v1/doctors/reservations",
                "/api/v1/doctors/profile",
                "/api/v1/doctors/schedules",
                "/api/v1/admin/reservations"
        );
    }

    private CustomUserDetails userDetails(Long userId, UserRole role) {
        User user = User.create(role.name().toLowerCase() + userId + "@test.com", "password", role);
        ReflectionTestUtils.setField(user, "id", userId);
        return new CustomUserDetails(user);
    }
}
