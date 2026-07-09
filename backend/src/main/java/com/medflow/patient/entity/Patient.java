package com.medflow.patient.entity;

import com.medflow.user.entity.User;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "birth",nullable = false)
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender",nullable = false, length = 10)
    private Gender gender;

    @Column(name = "phone",nullable = false, length = 20)
    private String phone;

    public Patient(
            User user,
            String name,
            LocalDate birth,
            Gender gender,
            String phone
    ) {
        this.user = user;
        this.name = name;
        this.birth = birth;
        this.gender = gender;
        this.phone = phone;
    }

    public static Patient create(
            User user,
            String name,
            LocalDate birth,
            Gender gender,
            String phone
    ) {
        return new Patient(user, name, birth, gender, phone);
    }

    public void update(
            String name,
            LocalDate birth,
            Gender gender,
            String phone
    ) {
        this.name = name;
        this.birth = birth;
        this.gender = gender;
        this.phone = phone;
    }
}
