package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.SubmissionEntity;

@Repository
public interface SubmissionRepository extends JpaRepository<SubmissionEntity, UUID> {

    Optional<SubmissionEntity> findByAttemptId(UUID attemptId);

    @Query("SELECT s FROM SubmissionEntity s " +
           "WHERE s.attempt.assessment.id = :assessmentId AND s.gradedAt IS NULL " +
           "ORDER BY s.createdAt ASC")
    List<SubmissionEntity> findPendingByAssessmentId(@Param("assessmentId") UUID assessmentId);

    /** Pending submissions across all courses where the given user is owner or instructor. */
    @Query("SELECT s FROM SubmissionEntity s " +
           "WHERE s.gradedAt IS NULL " +
           "AND (s.attempt.assessment.lesson.section.course.createdBy.id = :userId " +
           "     OR EXISTS (SELECT 1 FROM CourseInstructorEntity ci " +
           "                WHERE ci.course.id = s.attempt.assessment.lesson.section.course.id " +
           "                AND ci.instructor.id = :userId)) " +
           "ORDER BY s.createdAt ASC")
    List<SubmissionEntity> findPendingForGrader(@Param("userId") UUID userId);

    /** Pending submissions globally — admin only. */
    @Query("SELECT s FROM SubmissionEntity s " +
           "WHERE s.gradedAt IS NULL " +
           "ORDER BY s.createdAt ASC")
    List<SubmissionEntity> findAllPending();
}
