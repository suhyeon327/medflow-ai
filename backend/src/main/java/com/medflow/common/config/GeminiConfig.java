package com.medflow.common.config;

import com.google.genai.Client;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiConfig {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
    public Client geminiClient(GeminiProperties properties) {

        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new BusinessException(ErrorCode.GEMINI_API_KEY_NOT_CONFIGURED);
        }

        return Client.builder()
                .apiKey(properties.apiKey())
                .build();
    }
}
