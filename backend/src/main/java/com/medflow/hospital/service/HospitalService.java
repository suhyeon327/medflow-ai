package com.medflow.hospital.service;

import com.medflow.hospital.dto.HospitalRequest;
import com.medflow.hospital.dto.HospitalResponse;

public interface HospitalService {

    // 병원 등록
    HospitalResponse createHospital(HospitalRequest request);
}
