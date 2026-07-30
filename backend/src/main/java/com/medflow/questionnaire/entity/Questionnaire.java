package com.medflow.questionnaire.entity;

import com.medflow.common.entity.BaseEntity;
import com.medflow.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "questionnaires",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_questionnaire_reservation",
                columnNames = "reservation_id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Questionnaire extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, length = 200)
    private String chiefComplaint;   // 주 증상

    @Column(nullable = false)
    private LocalDateTime symptomStartedAt;   // 증상 시작 시점

    @Column(nullable = false, columnDefinition = "TEXT")   // 증상 상세 설명
    private String symptomDescription;

    private Integer painLevel;   // 통증 정도(0~10)

    @Column(precision = 4, scale = 1)
    private BigDecimal temperature;   // 체온

    @Column(columnDefinition = "TEXT")
    private String associatedSymptoms;   // 동반 증상

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;   // 기저질환

    @Column(columnDefinition = "TEXT")
    private String medications;   // 복용 중인 약

    @Column(columnDefinition = "TEXT")
    private String allergies;   // 알레르기

    @Column(columnDefinition = "TEXT")
    private String additionalNote;   // 추가 전달사항

    private Questionnaire(
            Reservation reservation,
            String chiefComplaint,
            LocalDateTime symptomStartedAt,
            String symptomDescription,
            Integer painLevel,
            BigDecimal temperature,
            String associatedSymptoms,
            String medicalHistory,
            String medications,
            String allergies,
            String additionalNote
    ) {
        this.reservation = reservation;
        this.chiefComplaint = chiefComplaint;
        this.symptomStartedAt = symptomStartedAt;
        this.symptomDescription = symptomDescription;
        this.painLevel = painLevel;
        this.temperature = temperature;
        this.associatedSymptoms = associatedSymptoms;
        this.medicalHistory = medicalHistory;
        this.medications = medications;
        this.allergies = allergies;
        this.additionalNote = additionalNote;
    }

    // 예약에 연결된 문진 생성
    public static Questionnaire create(
            Reservation reservation,
            String chiefComplaint,
            LocalDateTime symptomStartedAt,
            String symptomDescription,
            Integer painLevel,
            BigDecimal temperature,
            String associatedSymptoms,
            String medicalHistory,
            String medications,
            String allergies,
            String additionalNote
    ) {
        return new Questionnaire(
                reservation,
                chiefComplaint,
                symptomStartedAt,
                symptomDescription,
                painLevel,
                temperature,
                associatedSymptoms,
                medicalHistory,
                medications,
                allergies,
                additionalNote
        );
    }
}
