package com.medflow.hospital.entity;

import com.medflow.common.entity.BaseEntity;
import com.medflow.patient.entity.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "hospital")
@NoArgsConstructor
public class Hospital extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "tel", nullable = false, length = 20)
    private String tel;

    public Hospital(
            String name,
            String address,
            String region,
            String tel
    ) {
        this.name = name;
        this.address = address;
        this.region = region;
        this.tel = tel;
    }

    public static Hospital create(
            String name,
            String address,
            String region,
            String tel
    ) {
        return new Hospital(name, address, region, tel);
    }

    public void update(
            String name,
            String address,
            String region,
            String tel
    ) {
        this.name = name;
        this.address = address;
        this.region = region;
        this.tel = tel;
    }
}
