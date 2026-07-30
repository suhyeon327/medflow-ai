package com.medflow.questionnaire.repository;

import com.medflow.questionnaire.entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {

    boolean existsByReservationId(Long reservationId);
}
