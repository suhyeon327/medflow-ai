package com.medflow.hospital.entity;

import com.medflow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "hospitals")
@NoArgsConstructor
public class Hospital extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "tel", nullable = false, length = 20)
    private String tel;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private HospitalStatus status;

    public Hospital(
            String name,
            String address,
            String region,
            String tel,
            HospitalStatus status
    ) {
        this.name = name;
        this.address = address;
        this.region = region;
        this.tel = tel;
        this.status = status;
    }

    public static Hospital create(
            String name,
            String address,
            String region,
            String tel
    ) {
        return new Hospital(
                name,
                address,
                region,
                tel,
                HospitalStatus.ACTIVE
        );
    }

    public void update(
            String name,
            String address,
            String region,
            String tel,
            HospitalStatus status
    ) {
        this.name = name;
        this.address = address;
        this.region = region;
        this.tel = tel;
        this.status = status;
    }

    // 병원 삭제
    public void delete() {
        this.status = HospitalStatus.CLOSED;
        this.softDelete();
    }
}
