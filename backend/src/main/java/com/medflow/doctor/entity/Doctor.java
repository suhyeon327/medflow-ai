package com.medflow.doctor.entity;

import com.medflow.common.entity.BaseEntity;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.hospital.entity.Hospital;
import com.medflow.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "doctor")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Doctor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "department_id", nullable = false, unique = true)
//    private Department department;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "license_number", nullable = false, unique = true, length = 30)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoctorStatus status;

    private Doctor(
            User user,
            Hospital hospital,
//            Department department,
            String name,
            String licenseNumber
    ) {
        this.user = user;
        this.hospital = hospital;
//        this.department = department;
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.status = DoctorStatus.PENDING;
    }

    public static Doctor create(
            User user,
            Hospital hospital,
//            Department department,
            String name,
            String licenseNumber
    ) {
//        return new Doctor(user, hospital, department, name, licenseNumber);
        return new Doctor(user, hospital, name, licenseNumber);
    }

    // 의사 정보 수정
    public void update(Hospital hospital, String name, String licenseNumber) {

        if (this.status != DoctorStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_DOCTOR_STATUS);
        }

        this.hospital = hospital;
        this.name = name;
        this.licenseNumber = licenseNumber;
    }

    public void approve() {

        if (status != DoctorStatus.PENDING) {
            throw new IllegalStateException();
        }

        this.status = DoctorStatus.ACTIVE;
    }

    public void reject() {
        this.status = DoctorStatus.REJECTED;
    }

    public void deactivate() {
        this.status = DoctorStatus.INACTIVE;
    }
}
