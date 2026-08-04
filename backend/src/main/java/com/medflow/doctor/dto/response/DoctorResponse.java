package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorResponse {

    private Long doctorId;
    private String doctorName;
    private Long hospitalId;
    private String hospitalName;
    private String specialty;
    private String introduction;
    private String contact;

    public static DoctorResponse from(Doctor doctor) {
        return DoctorResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .hospitalId(doctor.getHospital().getId())
                .hospitalName(doctor.getHospital().getName())
                .specialty(doctor.getSpecialty())
                .introduction(doctor.getIntroduction())
                .contact(doctor.getContact() != null
                        ? doctor.getContact()
                        : doctor.getHospital().getTel())
                .build();
    }
}
