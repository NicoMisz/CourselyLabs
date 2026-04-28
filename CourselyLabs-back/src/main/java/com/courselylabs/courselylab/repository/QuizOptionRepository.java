package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.QuizOptionEntity;

@Repository
public interface QuizOptionRepository extends JpaRepository<QuizOptionEntity, UUID> {

    List<QuizOptionEntity> findByQuestionIdOrderByPositionAsc(UUID questionId);

    void deleteByQuestionId(UUID questionId);
}
