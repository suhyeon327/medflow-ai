package com.medflow.doctor.controller;

import com.medflow.auth.jwt.JwtProvider;
import com.medflow.common.config.SecurityConfig;
import com.medflow.common.security.CustomAuthenticationEntryPoint;
import com.medflow.doctor.dto.response.AvailableDoctorScheduleResponse;
import com.medflow.doctor.dto.response.DoctorDetailResponse;
import com.medflow.doctor.service.PublicDoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicDoctorController.class)
@ContextConfiguration(classes = {PublicDoctorController.class, SecurityConfig.class})
class PublicDoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicDoctorService publicDoctorService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Test
    void getDoctor_withoutAuthentication_returnsDoctor() throws Exception {
        Long doctorId = 1L;
        DoctorDetailResponse response = new DoctorDetailResponse(
                doctorId,
                "홍길동",
                10L,
                "메드플로우 병원",
                "내과",
                "환자 중심 진료를 제공합니다.",
                "02-1234-5678"
        );

        when(publicDoctorService.getDoctor(doctorId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/doctors/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.doctorId").value(doctorId))
                .andExpect(jsonPath("$.data.doctorName").value("홍길동"));
    }

    @Test
    void getAvailableDoctorSchedules_withoutAuthentication_returnsSchedules() throws Exception {
        Long doctorId = 1L;
        LocalDate date = LocalDate.of(2026, 8, 10);
        AvailableDoctorScheduleResponse response = new AvailableDoctorScheduleResponse(
                100L,
                date,
                LocalTime.of(9, 0),
                LocalTime.of(9, 30)
        );
        when(publicDoctorService.getAvailableDoctorSchedules(doctorId, date))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/doctors/{doctorId}/available-schedules", doctorId)
                        .param("date", "2026-08-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].scheduleId").value(100L))
                .andExpect(jsonPath("$.data[0].date").value("2026-08-10"));
    }
}
