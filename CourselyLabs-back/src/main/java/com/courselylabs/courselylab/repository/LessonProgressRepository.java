package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.LessonProgressEntity;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgressEntity, UUID> {

    Optional<LessonProgressEntity> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    List<LessonProgressEntity> findByUserIdAndIsCompletedTrue(UUID userId);

    @Query("SELECT lp FROM LessonProgressEntity lp " +
           "WHERE lp.user.id = :userId AND lp.lesson.section.course.id = :courseId")
    List<LessonProgressEntity> findByUserIdAndCourseId(
            @Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("SELECT COUNT(lp) FROM LessonProgressEntity lp " +
           "WHERE lp.user.id = :userId AND lp.lesson.section.course.id = :courseId AND lp.isCompleted = true")
    int countCompletedByUserIdAndCourseId(
            @Param("userId") UUID userId, @Param("courseId") UUID courseId);
}
