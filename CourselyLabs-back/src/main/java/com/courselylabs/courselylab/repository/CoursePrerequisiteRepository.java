package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.courselylabs.courselylab.entity.CoursePrerequisiteEntity;

public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisiteEntity, UUID> {

    List<CoursePrerequisiteEntity> findByCourseId(UUID courseId);

    Optional<CoursePrerequisiteEntity> findByCourseIdAndPrerequisiteCourseId(UUID courseId, UUID prerequisiteCourseId);

    boolean existsByCourseIdAndPrerequisiteCourseId(UUID courseId, UUID prerequisiteCourseId);
    
    // void deleteByCourseIdAndPrerequisiteCourseId(UUID courseId, UUID
    // prerequisiteCourseId);

    long countByCourseId(UUID courseId);

    @Query("""
        SELECT cp
        FROM CoursePrerequisiteEntity cp
        JOIN FETCH cp.prerequisiteCourse
        WHERE cp.course.id = :courseId
    """)
    List<CoursePrerequisiteEntity> findWithPrerequisiteCourseByCourseId(@Param("courseId") UUID courseId);

    @Query("""
        SELECT cp
        FROM CoursePrerequisiteEntity cp
        JOIN FETCH cp.course
        WHERE cp.prerequisiteCourse.id = :courseId
    """)
    List<CoursePrerequisiteEntity> findWithCourseByPrerequisiteCourseId(@Param("courseId") UUID courseId);
}
