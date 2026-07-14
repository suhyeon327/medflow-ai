package com.medflow.hospital.service;

import com.medflow.hospital.dto.AdminHospitalResponse;
import com.medflow.hospital.dto.HospitalRequest;
import com.medflow.hospital.dto.HospitalResponse;

import java.util.List;

public interface HospitalService {

    // 병원 등록
    HospitalResponse createHospital(HospitalRequest request);

    // 병원 관리 목록 조회
    List<AdminHospitalResponse> getHospitals();
}
