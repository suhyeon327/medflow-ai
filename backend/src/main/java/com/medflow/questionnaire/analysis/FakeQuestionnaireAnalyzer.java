package com.medflow.questionnaire.analysis;

import com.medflow.questionnaire.dto.request.QuestionnaireAnalysisRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisResponse;
import com.medflow.questionnaire.entity.PriorityLevel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        name = "ai.provider",
        havingValue = "fake",
        matchIfMissing = true
)
public class FakeQuestionnaireAnalyzer implements AiQuestionnaireAnalyzer {

    @Override
    public QuestionnaireAnalysisResponse analyze(QuestionnaireAnalysisRequest request) {
        String summary = "주 증상 '%s', 통증 정도 %s, 체온 %s로 입력되었으며 상세 증상은 '%s'입니다."
                .formatted(
                        request.chiefComplaint(),
                        valueOrNotProvided(request.painLevel()),
                        valueOrNotProvided(request.temperature()),
                        request.symptomDescription()
                );

        return new QuestionnaireAnalysisResponse(
                summary,
                List.of(
                        "주 증상과 상세 증상의 연관성을 확인해 주세요.",
                        "통증 정도와 체온 정보를 함께 확인해 주세요."
                ),
                List.of("기저질환, 복용약 및 알레르기 정보를 종합적으로 확인해 주세요."),
                List.of(
                        "증상의 시작 시점과 변화 양상을 추가로 확인해 주세요.",
                        "동반 증상의 지속 여부를 확인해 주세요."
                ),
                PriorityLevel.NORMAL
        );
    }

    private String valueOrNotProvided(Object value) {
        return value == null ? "미입력" : value.toString();
    }
}
