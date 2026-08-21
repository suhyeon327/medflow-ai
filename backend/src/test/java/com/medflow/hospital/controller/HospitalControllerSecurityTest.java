package com.medflow.hospital.controller;

import com.medflow.auth.jwt.JwtProvider;
import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.config.SecurityConfig;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.common.security.CustomAuthenticationEntryPoint;
import com.medflow.hospital.dto.response.HospitalPageResponse;
import com.medflow.hospital.dto.response.HospitalSummaryResponse;
import com.medflow.hospital.service.AdminHospitalService;
import com.medflow.hospital.service.HospitalService;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({HospitalController.class, AdminHospitalController.class})
@ContextConfiguration(classes = {
        HospitalController.class,
        AdminHospitalController.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        CustomAuthenticationEntryPoint.class
})
class HospitalControllerSecurityTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean HospitalService hospitalService;
    @MockitoBean AdminHospitalService adminHospitalService;
    @MockitoBean JwtProvider jwtProvider;

    @Test
    void hospitalList_isPublicAndReturnsPaginationResponse() throws Exception {
        HospitalPageResponse response = new HospitalPageResponse(
                List.of(), 1, 20, 21, 2, false, true
        );
        when(hospitalService.getAvailableHospitals(eq("서울"), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/hospitals")
                        .param("keyword", "서울")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(21))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.first").value(false))
                .andExpect(jsonPath("$.data.last").value(true));

        verify(hospitalService).getAvailableHospitals(
                eq("서울"),
                any(Pageable.class)
        );
    }

    @Test
    void hospitalList_withoutPagingParameter_usesDefaultPageSizeFifteen() throws Exception {
        when(hospitalService.getAvailableHospitals(eq(null), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(1);
                    return new HospitalPageResponse(
                            List.of(),
                            pageable.getPageNumber(),
                            pageable.getPageSize(),
                            0,
                            0,
                            true,
                            true
                    );
                });

        mockMvc.perform(get("/api/v1/hospitals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(15));
    }

    @Test
    void hospitalDetail_isPublic() throws Exception {
        mockMvc.perform(get("/api/v1/hospitals/{hospitalId}", 1L))
                .andExpect(status().isOk());

        verify(hospitalService).getDetailHospital(1L);
    }

    @Test
    void hospitalSummary_isPublicAndReturnsActiveHospitalAndDoctorCounts() throws Exception {
        when(hospitalService.getSummary()).thenReturn(new HospitalSummaryResponse(3L, 12L));

        mockMvc.perform(get("/api/v1/hospitals/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hospitalCount").value(3))
                .andExpect(jsonPath("$.data.doctorCount").value(12));

        verify(hospitalService).getSummary();
    }

    @Test
    void adminCanAccessHospitalManagementApi() throws Exception {
        when(adminHospitalService.getHospitals()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/hospitals")
                        .with(user(userDetails(1L, UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"PATIENT", "DOCTOR"})
    void generalUsersCannotAccessAdminHospitalApi(UserRole role) throws Exception {
        mockMvc.perform(get("/api/v1/admin/hospitals")
                        .with(user(userDetails(10L, role))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_006"));
    }

    @Test
    void unauthenticatedUserCannotAccessAdminHospitalApi() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/hospitals/{hospitalId}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_007"));
    }

    @Test
    void createHospital_rejectsMissingRequiredValue() throws Exception {
        mockMvc.perform(post("/api/v1/admin/hospitals")
                        .with(user(userDetails(1L, UserRole.ADMIN)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "address": "서울시 강남구",
                                  "region": "서울",
                                  "tel": "02-1234-5678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void updateHospital_rejectsMissingStatus() throws Exception {
        mockMvc.perform(put("/api/v1/admin/hospitals/{hospitalId}", 1L)
                        .with(user(userDetails(1L, UserRole.ADMIN)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "메드플로우 병원",
                                  "address": "서울시 강남구",
                                  "region": "서울",
                                  "tel": "02-1234-5678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void hospitalDetail_whenHospitalDoesNotExist_returnsHospitalNotFound() throws Exception {
        when(hospitalService.getDetailHospital(999L))
                .thenThrow(new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        mockMvc.perform(get("/api/v1/hospitals/{hospitalId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("HOSPITAL_002"));
    }

    @Test
    void createHospital_whenNameAlreadyExists_returnsConflict() throws Exception {
        when(adminHospitalService.createHospital(any()))
                .thenThrow(new BusinessException(ErrorCode.HOSPITAL_ALREADY_EXISTS));

        mockMvc.perform(post("/api/v1/admin/hospitals")
                        .with(user(userDetails(1L, UserRole.ADMIN)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "중복 병원",
                                  "address": "서울시 강남구",
                                  "region": "서울",
                                  "tel": "02-1234-5678"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("HOSPITAL_001"));
    }

    private CustomUserDetails userDetails(Long userId, UserRole role) {
        User user = User.create(role.name().toLowerCase() + userId + "@test.com", "password", role);
        ReflectionTestUtils.setField(user, "id", userId);
        return new CustomUserDetails(user);
    }
}
