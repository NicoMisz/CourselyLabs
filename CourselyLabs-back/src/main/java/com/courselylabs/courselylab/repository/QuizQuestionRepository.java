package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.QuizQuestionEntity;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestionEntity, UUID> {

    List<QuizQuestionEntity> findByAssessmentIdOrderByPositionAsc(UUID assessmentId);
}
