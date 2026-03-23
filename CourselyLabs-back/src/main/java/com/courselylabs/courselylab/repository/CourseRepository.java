package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.CourseEntity;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, UUID>, JpaSpecificationExecutor<CourseEntity> {

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

   /*  @Query("""
        SELECT c
        FROM CourseEntity c
        LEFT JOIN EnrollmentEntity e ON e.course.id = c.id
        LEFT JOIN ReviewEntity r ON r.course.id = c.id
        WHERE c.isPublished = true
            AND (:keyword IS NULL OR LOWER(c.title) LIKE CONCAT('%', :keyword, '%')
                        OR LOWER(c.description) LIKE CONCAT('%', :keyword, '%'))
            AND (:categoryId IS NULL OR c.category.id = :categoryId)
            AND (:level IS NULL OR LOWER(c.level) = :level)
            AND (:isFree IS NULL OR c.isFree = :isFree)
        GROUP BY c
        HAVING (:minRating IS NULL OR COALESCE(AVG(r.rating), 0) >= :minRating)
        ORDER BY COUNT(e.id) DESC, c.createdAt DESC
        """)
    Page<CourseEntity> searchPublishedPopular(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("level") String level,
            @Param("isFree") Boolean isFree,
            @Param("minRating") Double minRating,
            Pageable pageable);

    @Query("""
        SELECT c
        FROM CourseEntity c
        LEFT JOIN ReviewEntity r ON r.course.id = c.id
        WHERE c.isPublished = true
            AND (:keyword IS NULL OR LOWER(c.title) LIKE CONCAT('%', :keyword, '%')
                        OR LOWER(c.description) LIKE CONCAT('%', :keyword, '%'))
            AND (:categoryId IS NULL OR c.category.id = :categoryId)
            AND (:level IS NULL OR LOWER(c.level) = :level)
            AND (:isFree IS NULL OR c.isFree = :isFree)
        GROUP BY c
        HAVING (:minRating IS NULL OR COALESCE(AVG(r.rating), 0) >= :minRating)
        ORDER BY COALESCE(AVG(r.rating), 0) DESC, c.createdAt DESC
        """)
    Page<CourseEntity> searchPublishedRating(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("level") String level,
            @Param("isFree") Boolean isFree,
            @Param("minRating") Double minRating,
            Pageable pageable); */
}
