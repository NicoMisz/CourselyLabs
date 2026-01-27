package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.EnrollmentEntity;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, UUID> {
    Optional<EnrollmentEntity> findById(UUID id);
    
    List<EnrollmentEntity> findByUserId(UUID userId);

    //TODO quiero entender
    Page<EnrollmentEntity> findByUserId(UUID userId, Pageable pageable);

    List<EnrollmentEntity> findByCourseId(UUID courseId);

    //TODO quiero entender
    Page<EnrollmentEntity> findByCourseId(UUID courseId, Pageable pageable);

    Optional<EnrollmentEntity> findByUserIdAndCourseId(UUID userId, UUID courseId);

    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);

    long countByCourseId(UUID courseId);

    List<EnrollmentEntity> findByAccessType(String accessType);
}
