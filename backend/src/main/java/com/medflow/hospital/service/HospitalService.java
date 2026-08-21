package com.medflow.hospital.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.dto.response.DoctorResponse;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.dto.response.HospitalDetailResponse;
import com.medflow.hospital.dto.response.HospitalListResponse;
import com.medflow.hospital.dto.response.HospitalPageResponse;
import com.medflow.hospital.dto.response.HospitalSummaryResponse;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import com.medflow.hospital.repository.HospitalRepository;
import com.medflow.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;

    // 병원 목록 조회
    @Transactional(readOnly = true)
    public HospitalPageResponse getAvailableHospitals(String keyword, Pageable pageable) {

        // 병원 조회
        Page<Hospital> hospitals = keyword == null || keyword.isBlank()
                ? hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE, pageable)
                : hospitalRepository.searchByStatusAndKeyword(
                HospitalStatus.ACTIVE,
                keyword.trim(),
                pageable
        );

        // 조회된 병원들의 ID 추출
        List<Long> hospitalIds = hospitals.getContent().stream()
                .map(Hospital::getId)
                .toList();

        // 조회된 병원들에 소속된 활성 의사 조회
        List<Doctor> doctors = hospitalIds.isEmpty()
                ? List.of()
                : doctorRepository.findAllByHospitalIdInAndStatusAndUserStatus(
                hospitalIds,
                DoctorStatus.ACTIVE,
                UserStatus.ACTIVE
        );

        // 조회된 의사들을 병원 ID별로 그룹화
        Map<Long, List<Doctor>> doctorsByHospitalId = doctors.stream()
                .collect(Collectors.groupingBy(
                        doctor -> doctor.getHospital().getId()
                ));

        // 각 병원과 해당 병원의 의사 목록을 HospitalListResponse로 반환
        Page<HospitalListResponse> responses = hospitals.map(hospital -> HospitalListResponse.from(
                hospital,
                doctorsByHospitalId.getOrDefault(
                        hospital.getId(),
                        List.of()
                )
        ));

        // 병원 목록과 페이징 정보를 최종 응답 DTO로 변환
        return HospitalPageResponse.from(responses);
    }

    // 병원 상세 정보 조회
    @Transactional(readOnly = true)
    public HospitalDetailResponse getDetailHospital(Long hospitalId) {

        Hospital hospital = hospitalRepository.findByIdAndStatus(hospitalId, HospitalStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        return HospitalDetailResponse.from(hospital);
    }

    // 병원별 의사 목록 조회
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAvailableDoctors(Long hospitalId) {

        hospitalRepository.findByIdAndStatus(hospitalId, HospitalStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        List<Doctor> doctors = doctorRepository.findAllByHospitalIdAndStatusAndUserStatus(
                hospitalId,
                DoctorStatus.ACTIVE,
                UserStatus.ACTIVE
        );

        return doctors.stream()
                .map(DoctorResponse::from)
                .toList();
    }

    // 전체 병원 및 의료진 통계 조회
    public HospitalSummaryResponse getSummary() {

        long hospitalCount = hospitalRepository.countByStatus(HospitalStatus.ACTIVE);
        long doctorCount = doctorRepository.countByStatusAndUserStatus(
                DoctorStatus.ACTIVE,
                UserStatus.ACTIVE
        );

        return new HospitalSummaryResponse(
                hospitalCount,
                doctorCount
        );
    }
}
