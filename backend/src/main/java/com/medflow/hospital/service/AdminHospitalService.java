package com.medflow.hospital.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.hospital.dto.request.AdminHospitalCreateRequest;
import com.medflow.hospital.dto.request.AdminHospitalUpdateRequest;
import com.medflow.hospital.dto.response.AdminHospitalDeleteResponse;
import com.medflow.hospital.dto.response.AdminHospitalResponse;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminHospitalService {

    private final HospitalRepository hospitalRepository;

    // 병원 등록
    public AdminHospitalResponse createHospital(AdminHospitalCreateRequest request) {

        if (hospitalRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.HOSPITAL_ALREADY_EXISTS);
        }

        Hospital hospital = Hospital.create(
                request.name(),
                request.address(),
                request.region(),
                request.tel()
        );

        Hospital savedHospital = hospitalRepository.save(hospital);

        return AdminHospitalResponse.from(savedHospital);
    }

    // 병원 관리 목록 조회
    @Transactional(readOnly = true)
    public List<AdminHospitalResponse> getHospitals() {

        return hospitalRepository.findAll()
                .stream()
                .map(AdminHospitalResponse::from)
                .toList();
    }

    // 병원 정보 수정
    public AdminHospitalResponse updateHospital(
            Long hospitalId,
            AdminHospitalUpdateRequest request
    ) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        if (!hospital.getName().equals(request.name()) && hospitalRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.HOSPITAL_ALREADY_EXISTS);
        }

        hospital.update(
                request.name(),
                request.address(),
                request.region(),
                request.tel(),
                request.status()
        );

        return AdminHospitalResponse.from(hospital);
    }

    // 병원 삭제
    public AdminHospitalDeleteResponse deleteHospital(Long hospitalId) {

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        hospital.delete();

        return AdminHospitalDeleteResponse.from(hospital);
    }
}
