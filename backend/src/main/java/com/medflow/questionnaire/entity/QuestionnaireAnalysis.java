package com.medflow.questionnaire.entity;

import com.medflow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "questionnaire_analyses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_questionnaire_analysis_questionnaire",
                columnNames = "questionnaire_id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionnaireAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionnaireAnalysisStatus status;

    private QuestionnaireAnalysis(Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
        this.status = QuestionnaireAnalysisStatus.PENDING;
    }

    // 분석 대기 상태 생성
    public static QuestionnaireAnalysis pending(Questionnaire questionnaire) {
        return new QuestionnaireAnalysis(questionnaire);
    }

    // 문진 변경에 따른 재분석 대기 처리
    public void resetToPending() {
        this.status = QuestionnaireAnalysisStatus.PENDING;
    }
}
