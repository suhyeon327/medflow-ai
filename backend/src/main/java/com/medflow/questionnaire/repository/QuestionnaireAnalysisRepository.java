package com.medflow.questionnaire.repository;

import com.medflow.questionnaire.entity.QuestionnaireAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionnaireAnalysisRepository extends JpaRepository<QuestionnaireAnalysis, Long> {

    Optional<QuestionnaireAnalysis> findByQuestionnaireId(Long questionnaireId);
}
