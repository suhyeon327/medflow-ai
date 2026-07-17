package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorUpdateResponse {

    private Long doctorId;

    private String doctorName;

    private DoctorStatus doctorstatus;

    public static DoctorUpdateResponse from(Doctor doctor) {

        return DoctorUpdateResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .doctorstatus(doctor.getStatus())
                .build();
    }
}
