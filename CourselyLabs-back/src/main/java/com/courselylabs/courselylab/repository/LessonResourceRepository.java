package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.LessonResourceEntity;

@Repository
public interface LessonResourceRepository extends JpaRepository<LessonResourceEntity, UUID> {

    List<LessonResourceEntity> findByLessonIdOrderByPositionAsc(UUID lessonId);

    boolean existsByStorageKey(String storageKey);
}
