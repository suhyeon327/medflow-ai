package com.medflow.questionnaire.event;

import com.medflow.questionnaire.service.QuestionnaireAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionnaireAnalysisEventListener {

    private final QuestionnaireAnalysisService questionnaireAnalysisService;

    // 문진 저장 트랜잭션이 커밋된 후 별도 트랜잭션으로 분석
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(QuestionnaireAnalysisRequestedEvent event) {
        try {
            questionnaireAnalysisService.analyze(event.questionnaireId());
        } catch (Exception e) {
            // 이미 저장된 문진 원본에 영향을 주지 않도록 이벤트 예외를 전파하지 않음
            log.error("문진 분석 처리 중 오류가 발생했습니다. questionnaireId={}", event.questionnaireId(), e);
        }
    }
}
