package com.medflow.questionnaire.repository;

import com.medflow.questionnaire.entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {

    boolean existsByReservationId(Long reservationId);

    Optional<Questionnaire> findByReservationId(Long reservationId);
}
