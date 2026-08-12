package com.medflow.hospital.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.common.exception.HospitalAlreadyExistsException;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.dto.response.DoctorResponse;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.dto.request.AdminHospitalCreateRequest;
import com.medflow.hospital.dto.request.AdminHospitalUpdateRequest;
import com.medflow.hospital.dto.response.AdminHospitalResponse;
import com.medflow.hospital.dto.response.HospitalDetailResponse;
import com.medflow.hospital.dto.response.HospitalListResponse;
import com.medflow.hospital.dto.response.AdminHospitalDeleteResponse;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import com.medflow.hospital.repository.HospitalRepository;
import com.medflow.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
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

    // 사용자 병원 목록 조회
    @Transactional(readOnly = true)
    public List<HospitalListResponse> getAvailableHospitals(String keyword) {

        List<Hospital> hospitals = keyword == null || keyword.isBlank()
                ? hospitalRepository.findAllByStatus(HospitalStatus.ACTIVE)
                : hospitalRepository.searchByStatusAndKeyword(
                HospitalStatus.ACTIVE,
                keyword.trim()
        );

        List<Long> hospitalIds = hospitals.stream()
                .map(Hospital::getId)
                .toList();

        List<Doctor> doctors = hospitalIds.isEmpty()
                ? List.of()
                : doctorRepository.findAllByHospitalIdInAndStatusAndUserStatus(
                hospitalIds,
                DoctorStatus.ACTIVE,
                UserStatus.ACTIVE
        );

        Map<Long, List<Doctor>> doctorsByHospitalId = doctors.stream()
                .collect(Collectors.groupingBy(
                        doctor -> doctor.getHospital().getId()
                ));

        return hospitals.stream()
                .map(hospital -> HospitalListResponse.of(
                        hospital,
                        doctorsByHospitalId.getOrDefault(
                                hospital.getId(),
                                List.of()
                        )
                ))
                .toList();
    }

    // 병원 상세 정보 조회
    @Transactional(readOnly = true)
    public HospitalDetailResponse getDetailHospital(Long hospitalId) {

        Hospital hospital = hospitalRepository.findByIdAndStatus(hospitalId, HospitalStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        return HospitalDetailResponse.from(hospital);
    }

    // 병원별 공개 의사 목록 조회
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAvailableDoctors(Long hospitalId) {
        hospitalRepository.findByIdAndStatus(hospitalId, HospitalStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        return doctorRepository.findAllByHospitalIdAndStatusAndUserStatus(
                        hospitalId,
                        DoctorStatus.ACTIVE,
                        UserStatus.ACTIVE
                ).stream()
                .map(DoctorResponse::from)
                .toList();
    }
}
