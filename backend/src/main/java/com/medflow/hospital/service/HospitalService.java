package com.medflow.hospital.service;

import com.medflow.hospital.dto.AdminHospitalResponse;
import com.medflow.hospital.dto.HospitalRequest;
import com.medflow.hospital.dto.HospitalResponse;

import java.util.List;

public interface HospitalService {

    // 관리자 기능

    // 병원 등록
    HospitalResponse createHospital(HospitalRequest request);

    // 병원 관리 목록 조회
    List<AdminHospitalResponse> getHospitals();

    // 병원 정보 수정
    AdminHospitalResponse updateHospital(Long hospitalId, HospitalRequest request);

    // 사용자 기능

    // 병원 목록 조회
    List<HospitalResponse> getAvailableHospitals();

    // 병원 상세 정보 조회
    HospitalResponse getDetailHospital(Long hospitalId);
}