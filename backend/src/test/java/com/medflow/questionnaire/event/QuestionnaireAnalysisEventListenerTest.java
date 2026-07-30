package com.medflow.questionnaire.event;

import com.medflow.questionnaire.service.QuestionnaireAnalysisService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class QuestionnaireAnalysisEventListenerTest {

    @Test
    void handle_doesNotPropagateAnalysisFailure() {
        QuestionnaireAnalysisService analysisService = mock(QuestionnaireAnalysisService.class);
        QuestionnaireAnalysisEventListener listener = new QuestionnaireAnalysisEventListener(analysisService);
        doThrow(new IllegalStateException("분석 저장 실패")).when(analysisService).analyze(20L);

        assertThatCode(() -> listener.handle(new QuestionnaireAnalysisRequestedEvent(20L)))
                .doesNotThrowAnyException();
        verify(analysisService).analyze(20L);
    }
}
