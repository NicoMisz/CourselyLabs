package com.courselylabs.courselylab.repository;

import com.courselylabs.courselylab.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    List<ReviewEntity> findByCourseId(UUID courseId);

    Page<ReviewEntity> findByCourseId(UUID courseId, Pageable pageable);

    List<ReviewEntity> findByUserId(UUID userId);

    Optional<ReviewEntity> findByCourseIdAndUserId(UUID courseId, UUID userId);

    boolean existsByCourseIdAndUserId(UUID courseId, UUID userId);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.course.id = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") UUID courseId);

    long countByCourseId(UUID courseId);

    List<ReviewEntity> findByCourseIdAndRating(UUID courseId, Integer rating);
}
