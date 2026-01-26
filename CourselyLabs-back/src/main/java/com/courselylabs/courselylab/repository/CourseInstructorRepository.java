package com.courselylabs.courselylab.repository;

import com.courselylabs.courselylab.entity.CourseInstructorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseInstructorRepository extends JpaRepository<CourseInstructorEntity, Integer> {

    List<CourseInstructorEntity> findByCourseId(UUID courseId);

    List<CourseInstructorEntity> findByInstructorId(UUID instructorId);

    Optional<CourseInstructorEntity> findByCourseIdAndInstructorId(UUID courseId, UUID instructorId);

    boolean existsByCourseIdAndInstructorId(UUID courseId, UUID instructorId);

    @Query("SELECT ci FROM CourseInstructorEntity ci WHERE ci.course.id = :courseId AND ci.isMain = true")
    Optional<CourseInstructorEntity> findMainInstructorByCourseId(@Param("courseId") UUID courseId);

    void deleteByCourseIdAndInstructorId(UUID courseId, UUID instructorId);
}
