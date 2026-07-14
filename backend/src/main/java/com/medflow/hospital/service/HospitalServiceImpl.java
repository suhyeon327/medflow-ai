package com.medflow.hospital.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.common.exception.HospitalAlreadyExistsException;
import com.medflow.hospital.dto.AdminHospitalResponse;
import com.medflow.hospital.dto.HospitalListResponse;
import com.medflow.hospital.dto.HospitalRequest;
import com.medflow.hospital.dto.HospitalDetailResponse;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;

    // 관리자 기능

    // 병원 등록
    @Override
    public HospitalDetailResponse createHospital(HospitalRequest request) {

        if (hospitalRepository.existsByName(request.getName())) {
            throw new HospitalAlreadyExistsException();
        }

        Hospital hospital = Hospital.create(
                request.getName(),
                request.getAddress(),
                request.getRegion(),
                request.getTel()
        );

        Hospital savedHospital = hospitalRepository.save(hospital);

        return HospitalDetailResponse.from(hospital);
    }

    // 병원 관리 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<AdminHospitalResponse> getHospitals() {

        return hospitalRepository.findAll()
                .stream()
                .map(AdminHospitalResponse::from)
                .toList();
    }

    // 병원 정보 수정
    @Override
    public AdminHospitalResponse updateHospital(
            Long hospitalId,
            HospitalRequest request) {

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        if (!hospital.getName().equals(request.getName()) && hospitalRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.HOSPITAL_ALREADY_EXISTS);
        }

        hospital.update(
                request.getName(),
                request.getAddress(),
                request.getRegion(),
                request.getTel()
        );

        return AdminHospitalResponse.from(hospital);
    }

    // 사용자 기능

    // 사용자 병원 목록 조회
    @Override
    public List<HospitalListResponse> getAvailableHospitals() {
        return hospitalRepository.findAll()
                .stream()
                .map(HospitalListResponse::from)
                .toList();
    }

    // 병원 상세 정보 조회
    public HospitalDetailResponse getDetailHospital(Long hospitalId) {

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        return HospitalDetailResponse.from(hospital);
    }
}
