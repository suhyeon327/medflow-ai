package com.medflow.hospital.service;

import com.medflow.common.exception.HospitalAlreadyExistsException;
import com.medflow.hospital.dto.AdminHospitalResponse;
import com.medflow.hospital.dto.HospitalRequest;
import com.medflow.hospital.dto.HospitalResponse;
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

    // 병원 등록
    @Override
    public HospitalResponse createHospital(HospitalRequest request) {

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

        return HospitalResponse.from(hospital);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminHospitalResponse> getHospitals() {

        return hospitalRepository.findAll()
                .stream()
                .map(AdminHospitalResponse::from)
                .toList();
    }
}
