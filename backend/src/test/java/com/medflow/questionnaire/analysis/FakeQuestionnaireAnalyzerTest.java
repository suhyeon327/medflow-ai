package com.medflow.questionnaire.analysis;

import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisResponse;
import com.medflow.questionnaire.entity.Questionnaire;
import com.medflow.questionnaire.entity.PriorityLevel;
import com.medflow.reservation.entity.Reservation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FakeQuestionnaireAnalyzerTest {

    private final FakeQuestionnaireAnalyzer analyzer = new FakeQuestionnaireAnalyzer();

    @Test
    void analyze_returnsPredictableResultContainingChiefComplaint() {
        Questionnaire questionnaire = Questionnaire.create(
                mock(Reservation.class), "복통", LocalDateTime.of(2026, 7, 28, 9, 0),
                "배가 아픕니다.", 6, new BigDecimal("37.5"), "메스꺼움",
                "고혈압", "혈압약", "없음", null
        );

        QuestionnaireAnalysisResponse result = analyzer.analyze(questionnaire);

        assertThat(result.summary()).isEqualTo(
                "주 증상 '복통', 통증 정도 6, 체온 37.5로 입력되었으며 상세 증상은 '배가 아픕니다.'입니다."
        );
        assertThat(result.keyFindings()).containsExactly(
                "주 증상과 상세 증상의 연관성을 확인해 주세요.",
                "통증 정도와 체온 정보를 함께 확인해 주세요."
        );
        assertThat(result.riskSignals())
                .containsExactly("기저질환, 복용약 및 알레르기 정보를 종합적으로 확인해 주세요.");
        assertThat(result.doctorCheckpoints()).containsExactly(
                "증상의 시작 시점과 변화 양상을 추가로 확인해 주세요.",
                "동반 증상의 지속 여부를 확인해 주세요."
        );
        assertThat(result.priorityLevel()).isEqualTo(PriorityLevel.NORMAL);
    }
}
