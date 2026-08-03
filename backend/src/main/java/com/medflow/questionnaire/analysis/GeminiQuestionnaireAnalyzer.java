package com.medflow.questionnaire.analysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.medflow.common.config.GeminiProperties;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.questionnaire.dto.analysis.GeminiAnalysisResult;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisResponse;
import com.medflow.questionnaire.entity.PriorityLevel;
import com.medflow.questionnaire.entity.Questionnaire;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiQuestionnaireAnalyzer implements AiQuestionnaireAnalyzer {

    // 시스템 지침
    private static final String SYSTEM_INSTRUCTION = """
            당신은 환자가 작성한 문진을 의료진이 빠르게 확인할 수 있도록 요약하고 구조화하는 보조 시스템입니다.

            다음 규칙을 반드시 지키세요.
            1. 질병을 진단하지 마세요.
            2. 치료 또는 처방을 제안하지 마세요.
            3. 입력에 없는 사실을 만들어내지 마세요.
            4. 불확실한 내용을 확정적으로 표현하지 마세요.
            5. priorityLevel은 응급도가 아니라 의료진 확인 우선순위입니다.
            6. priorityLevel은 NORMAL, CAUTION, HIGH_PRIORITY 중 하나만 사용하세요.
            7. 문진 원문을 단순 복사하지 말고 여러 항목을 종합하세요.
            """;

    private final Client geminiClient;   // Gemini API 실제 호출
    private final GeminiProperties geminiProperties;   // Gemini 설정
    private final ObjectMapper objectMapper;   // JSON 문자열 -> JAVA 객체

    @Override
    public QuestionnaireAnalysisResponse analyze(Questionnaire questionnaire) {

        // Gemini API 호출
        GenerateContentResponse response = geminiClient.models.generateContent(
                geminiProperties.model(),   // 사용할 모델
                createQuestionnairePrompt(questionnaire),   // 사용자 프롬프트
                createGenerateContentConfig()   // 응답설정
        );

        if (response == null) {
            log.error("Gemini 응답 객체가 null입니다.");
            throw new BusinessException(ErrorCode.AI_ANALYSIS_FAILED);
        }

        return convertResponse(response.text());
    }

    // JSON 응답 변환
    QuestionnaireAnalysisResponse convertResponse(String responseText) {

        if (responseText == null || responseText.isBlank()) {
            log.error("Gemini 응답 객체가 null이거나 비어있습니다.");
            throw new BusinessException(ErrorCode.AI_ANALYSIS_FAILED);
        }

        try {
            GeminiAnalysisResult result = objectMapper.readValue(responseText, GeminiAnalysisResult.class);
            validateResult(result);

            return new QuestionnaireAnalysisResponse(
                    result.summary(),
                    immutableOrEmpty(result.keyFindings()),
                    immutableOrEmpty(result.riskSignals()),
                    immutableOrEmpty(result.doctorCheckpoints()),
                    PriorityLevel.valueOf(result.priorityLevel())
            );
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AI_ANALYSIS_FAILED);
        }
    }

    private GenerateContentConfig createGenerateContentConfig() {

        Content systemInstruction = Content.fromParts(Part.fromText(SYSTEM_INSTRUCTION));

        return GenerateContentConfig.builder()
                .systemInstruction(systemInstruction)   // 시스템 지침
                .responseMimeType("application/json")   // JSON 형식으로 응답
                .responseJsonSchema(createResponseSchema())   // JSON이 가져야 할 필드와 구조
                .candidateCount(1)   // 응답 후보 개수
                .temperature(0.1f)   // 창의성 제한(temperature가 낮을 수록 답변이 일관적이고 보수적)
                .build();
    }

    // 응답 JSON 스키마
    private Map<String, Object> createResponseSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,   // 정의되지 않은 필드 생성 제한
                "properties", Map.of(
                        "summary", stringSchema("전체 문진 내용을 종합한 의료진 참고용 요약"),
                        "keyFindings", stringArraySchema("의료진이 빠르게 파악해야 할 핵심 정보"),
                        "riskSignals", stringArraySchema("의료진이 주의해서 확인해야 할 정보"),
                        "doctorCheckpoints", stringArraySchema("진료 시 추가로 확인할 질문 또는 사항"),
                        "priorityLevel", Map.of(
                                "type", "string",
                                "description", "응급도가 아닌 의료진 확인 우선순위",
                                "enum", List.of("NORMAL", "CAUTION", "HIGH_PRIORITY")
                        )
                ),
                "required", List.of(
                        "summary",
                        "keyFindings",
                        "riskSignals",
                        "doctorCheckpoints",
                        "priorityLevel"
                )
        );
    }

    private Map<String, Object> stringSchema(String description) {
        return Map.of("type", "string", "description", description);
    }

    private Map<String, Object> stringArraySchema(String description) {
        return Map.of(
                "type", "array",
                "description", description,
                "items", Map.of("type", "string")
        );
    }

    private String createQuestionnairePrompt(Questionnaire questionnaire) {
        return """
                다음 환자 문진을 분석해 주세요.

                주 증상: %s
                증상 시작 시점: %s
                증상 상세 설명: %s
                통증 정도: %s
                체온: %s
                동반 증상: %s
                기저질환: %s
                복용 중인 약: %s
                알레르기: %s
                추가 전달사항: %s
                """.formatted(
                questionnaire.getChiefComplaint(),
                questionnaire.getSymptomStartedAt(),
                questionnaire.getSymptomDescription(),
                valueOrNotProvided(questionnaire.getPainLevel()),
                valueOrNotProvided(questionnaire.getTemperature()),
                valueOrNotProvided(questionnaire.getAssociatedSymptoms()),
                valueOrNotProvided(questionnaire.getMedicalHistory()),
                valueOrNotProvided(questionnaire.getMedications()),
                valueOrNotProvided(questionnaire.getAllergies()),
                valueOrNotProvided(questionnaire.getAdditionalNote())
        );
    }

    private void validateResult(GeminiAnalysisResult result) {
        if (result == null
                || result.summary() == null
                || result.summary().isBlank()
                || result.priorityLevel() == null
                || result.priorityLevel().isBlank()) {
            throw new BusinessException(ErrorCode.AI_ANALYSIS_FAILED);
        }
    }

    private <T> List<T> immutableOrEmpty(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private String valueOrNotProvided(Object value) {
        return value == null ? "미입력" : value.toString();
    }
}
