package com.medflow.hospital.service;

import com.medflow.hospital.dto.request.HospitalCreateRequest;
import com.medflow.hospital.dto.request.HospitalUpdateRequest;
import com.medflow.hospital.dto.response.AdminHospitalResponse;
import com.medflow.hospital.dto.response.HospitalDetailResponse;
import com.medflow.hospital.dto.response.HospitalListResponse;
import com.medflow.hospital.dto.response.deleteResponse;

import java.util.List;

public interface HospitalService {

    // 관리자 기능

    // 병원 등록
    HospitalDetailResponse createHospital(HospitalCreateRequest request);

    // 병원 관리 목록 조회
    List<AdminHospitalResponse> getHospitals();

    // 병원 정보 수정
    AdminHospitalResponse updateHospital(Long hospitalId, HospitalUpdateRequest request);

    // 병원 삭제
    deleteResponse deleteHospital(Long hospitalId);

    // 사용자 기능

    // 병원 목록 조회
    List<HospitalListResponse> getAvailableHospitals();

    // 병원 상세 정보 조회
    HospitalDetailResponse getDetailHospital(Long hospitalId);
}