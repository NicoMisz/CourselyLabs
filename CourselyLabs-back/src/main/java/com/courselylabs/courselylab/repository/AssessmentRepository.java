package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.AssessmentEntity;

@Repository
public interface AssessmentRepository extends JpaRepository<AssessmentEntity, UUID> {

    Optional<AssessmentEntity> findByLessonId(UUID lessonId);

    @Query("SELECT a FROM AssessmentEntity a " +
           "WHERE a.lesson.section.course.id = :courseId AND a.type = :type")
    List<AssessmentEntity> findByCourseIdAndType(@Param("courseId") UUID courseId, @Param("type") String type);
}
