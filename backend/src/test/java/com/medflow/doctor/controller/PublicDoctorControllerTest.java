package com.medflow.doctor.controller;

import com.medflow.auth.jwt.JwtProvider;
import com.medflow.common.config.SecurityConfig;
import com.medflow.common.security.CustomAuthenticationEntryPoint;
import com.medflow.doctor.dto.response.DoctorResponse;
import com.medflow.doctor.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
    private DoctorService doctorService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Test
    void getDoctor_withoutAuthentication_returnsDoctor() throws Exception {
        Long doctorId = 1L;
        DoctorResponse response = DoctorResponse.builder()
                .doctorId(doctorId)
                .doctorName("홍길동")
                .hospitalId(10L)
                .hospitalName("메드플로우 병원")
                .specialty("내과")
                .build();

        when(doctorService.getPublicDoctor(doctorId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/doctors/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.doctorId").value(doctorId))
                .andExpect(jsonPath("$.data.doctorName").value("홍길동"));
    }
}
