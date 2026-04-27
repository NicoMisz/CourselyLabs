package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.AssessmentAttemptEntity;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttemptEntity, UUID> {

    List<AssessmentAttemptEntity> findByAssessmentIdAndUserIdOrderByAttemptNumberDesc(UUID assessmentId, UUID userId);

    Optional<AssessmentAttemptEntity> findFirstByAssessmentIdAndUserIdAndStatus(UUID assessmentId, UUID userId, String status);

    long countByAssessmentIdAndUserId(UUID assessmentId, UUID userId);

    List<AssessmentAttemptEntity> findByAssessmentIdAndStatusOrderByStartedAtDesc(UUID assessmentId, String status);
}
