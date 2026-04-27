package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.QuizAnswerEntity;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswerEntity, UUID> {

    List<QuizAnswerEntity> findByAttemptId(UUID attemptId);

    void deleteByAttemptId(UUID attemptId);
}
