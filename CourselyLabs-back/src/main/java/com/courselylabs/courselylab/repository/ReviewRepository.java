package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.ReviewEntity;

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

    @Query("SELECT r FROM ReviewEntity r WHERE r.course.id = :courseId AND r.user.email = :email")
    Optional<ReviewEntity> findByCourseIdAndUserEmail(
            @Param("courseId") UUID courseId,
            @Param("email") String email);

    Page<ReviewEntity> findByCourseIdOrderByCreatedAtDesc(UUID courseId, Pageable pageable);
    Page<ReviewEntity> findByCourseIdOrderByRatingDescCreatedAtDesc(UUID courseId, Pageable pageable);
    Page<ReviewEntity> findByCourseIdOrderByRatingAscCreatedAtDesc(UUID courseId, Pageable pageable);

}
