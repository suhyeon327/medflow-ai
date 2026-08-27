package com.medflow.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class QuestionnaireAsyncConfig {

    @Bean(name = "questionnaireAnalysisExecutor")
    public Executor questionnaireAnalysisExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);   // 기본적으로 동시에 2개의 분석 수행
        executor.setMaxPoolSize(4);   // 작업이 많이 밀리면 최대 4개까지 동시 수행
        executor.setQueueCapacity(20);   // 바로 처리하지 못한 분석 작업 최대 20개 대기
        executor.setThreadNamePrefix("questionnaire-analysis-");   // 로그에서 분석 스레드를 쉽게 구분

        executor.initialize();

        return executor;
    }
}
