package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.SectionEntity;

@Repository
public interface SectionRepository extends JpaRepository<SectionEntity, UUID> {

    List<SectionEntity> findByCourseIdOrderByPositionAsc(UUID courseId);

    int countByCourseId(UUID courseId);
}
