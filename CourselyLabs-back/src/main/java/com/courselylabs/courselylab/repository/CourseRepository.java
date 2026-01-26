package com.courselylabs.courselylab.repository;

import com.courselylabs.courselylab.entity.CourseEntity;
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
public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

    Optional<CourseEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<CourseEntity> findByIsPublishedTrue();

    Page<CourseEntity> findByIsPublishedTrue(Pageable pageable);

    List<CourseEntity> findByCategoryId(Integer categoryId);

    Page<CourseEntity> findByCategoryIdAndIsPublishedTrue(Integer categoryId, Pageable pageable);

    List<CourseEntity> findByStatus(String status);

    @Query("SELECT c FROM CourseEntity c WHERE c.isPublished = true AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<CourseEntity> searchPublishedCourses(@Param("keyword") String keyword, Pageable pageable);

    List<CourseEntity> findByIsFreeTrue();

    Page<CourseEntity> findByIsFreeAndIsPublishedTrue(Boolean isFree, Pageable pageable);
}
