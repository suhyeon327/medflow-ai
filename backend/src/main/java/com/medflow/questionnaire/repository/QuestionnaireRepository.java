package com.medflow.questionnaire.repository;

import com.medflow.questionnaire.entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {

    boolean existsByReservationId(Long reservationId);

    Optional<Questionnaire> findByReservationId(Long reservationId);

    @Query("""
            select q
            from Questionnaire q
            join fetch q.reservation r
            where r.id in :reservationIds
            """)
    List<Questionnaire> findAllByReservationIdIn(@Param("reservationIds") List<Long> reservationIds);
}
