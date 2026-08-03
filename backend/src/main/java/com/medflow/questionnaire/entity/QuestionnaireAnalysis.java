package com.medflow.questionnaire.entity;

import com.medflow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    private Questionnaire questionnaire;   // 분석 대상 문진

    @Column(columnDefinition = "TEXT")
    private String summary;   // 전체 문진 종합 요약

    @ElementCollection
    @CollectionTable(
            name = "questionnaire_analysis_key_findings",
            joinColumns = @JoinColumn(name = "analysis_id")
    )
    @Column(name = "key_finding", length = 500)
    private List<String> keyFindings = new ArrayList<>();   // 의료진이 빠르게 확인할 핵심 내용

    @ElementCollection   // 엔티티가 아닌 값 컬렉션을 저장
    @CollectionTable(   // ElementCollection을 저장할 테이블을 정의
            name = "questionnaire_analysis_risk_signals",   // 테이블명
            joinColumns = @JoinColumn(name = "analysis_id")
    )
    @Column(name = "risk_signal", length = 500)
    private List<String> riskSignals = new ArrayList<>();   // 의료진이 주의해서 확인할 정보

    @ElementCollection
    @CollectionTable(
            name = "questionnaire_analysis_doctor_checkpoints",
            joinColumns = @JoinColumn(name = "analysis_id")
    )
    @Column(name = "doctor_checkpoint", length = 500)
    private List<String> doctorCheckpoints = new ArrayList<>();   // 진료 시 추가로 확인할 사항

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PriorityLevel priorityLevel;   // 의료진 확인 우선순위

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionnaireAnalysisStatus status;   // 분석 진행 상태

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
        clearResult();
    }

    // 분석 시작
    public void startProcessing() {
        this.status = QuestionnaireAnalysisStatus.PROCESSING;
    }

    // 분석 성공 결과 반영
    public void complete(
            String summary,
            List<String> keyFindings,
            List<String> riskSignals,
            List<String> doctorCheckpoints,
            PriorityLevel priorityLevel
    ) {
        this.summary = summary;
        replaceElements(this.keyFindings, keyFindings);
        replaceElements(this.riskSignals, riskSignals);
        replaceElements(this.doctorCheckpoints, doctorCheckpoints);
        this.priorityLevel = priorityLevel;
        this.status = QuestionnaireAnalysisStatus.COMPLETED;
    }

    // 분석 실패 처리
    public void fail() {
        clearResult();
        this.status = QuestionnaireAnalysisStatus.FAILED;
    }

    private void clearResult() {
        this.summary = null;
        this.keyFindings.clear();
        this.riskSignals.clear();
        this.doctorCheckpoints.clear();
        this.priorityLevel = null;
    }

    private void replaceElements(List<String> target, List<String> values) {
        target.clear();
        target.addAll(values);
    }
}
