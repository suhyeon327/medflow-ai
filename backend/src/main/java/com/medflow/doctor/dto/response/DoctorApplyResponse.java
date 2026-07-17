package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorApplyResponse {

    private Long doctorId;

    private String doctorName;

    private DoctorStatus doctorstatus;

    public static DoctorApplyResponse from(Doctor doctor) {

        return DoctorApplyResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .doctorstatus(doctor.getStatus())
                .build();
    }
}
