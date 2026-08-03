package com.medflow.questionnaire.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medflow.common.config.GeminiProperties;
import com.medflow.common.exception.BusinessException;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisResponse;
import com.medflow.questionnaire.entity.PriorityLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiQuestionnaireAnalyzerTest {

    private final GeminiQuestionnaireAnalyzer analyzer = new GeminiQuestionnaireAnalyzer(
            null,
            new GeminiProperties("test-key", "gemini-test-model"),
            new ObjectMapper()
    );

    @Test
    void 구조화된_Gemini_응답을_분석_결과로_변환한다() {
        String responseText = """
                {
                  "summary": "기침과 발열이 지속되어 의료진 확인이 필요합니다.",
                  "keyFindings": ["3일간 지속된 기침", "38도의 체온"],
                  "riskSignals": ["발열 지속"],
                  "doctorCheckpoints": ["호흡곤란 여부 확인"],
                  "priorityLevel": "CAUTION"
                }
                """;

        QuestionnaireAnalysisResponse response = analyzer.convertResponse(responseText);

        assertThat(response.summary()).isEqualTo("기침과 발열이 지속되어 의료진 확인이 필요합니다.");
        assertThat(response.keyFindings()).containsExactly("3일간 지속된 기침", "38도의 체온");
        assertThat(response.riskSignals()).containsExactly("발열 지속");
        assertThat(response.doctorCheckpoints()).containsExactly("호흡곤란 여부 확인");
        assertThat(response.priorityLevel()).isEqualTo(PriorityLevel.CAUTION);
    }

    @Test
    void 목록이_null이면_빈_목록으로_변환한다() {
        String responseText = """
                {
                  "summary": "특이 위험 신호가 입력되지 않았습니다.",
                  "keyFindings": null,
                  "riskSignals": null,
                  "doctorCheckpoints": null,
                  "priorityLevel": "NORMAL"
                }
                """;

        QuestionnaireAnalysisResponse response = analyzer.convertResponse(responseText);

        assertThat(response.keyFindings()).isEqualTo(List.of());
        assertThat(response.riskSignals()).isEqualTo(List.of());
        assertThat(response.doctorCheckpoints()).isEqualTo(List.of());
    }

    @Test
    void 잘못된_JSON이면_분석_실패_예외가_발생한다() {
        assertThatThrownBy(() -> analyzer.convertResponse("not-json"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 허용되지_않은_우선순위이면_분석_실패_예외가_발생한다() {
        String responseText = """
                {
                  "summary": "요약",
                  "keyFindings": [],
                  "riskSignals": [],
                  "doctorCheckpoints": [],
                  "priorityLevel": "EMERGENCY"
                }
                """;

        assertThatThrownBy(() -> analyzer.convertResponse(responseText))
                .isInstanceOf(BusinessException.class);
    }
}
