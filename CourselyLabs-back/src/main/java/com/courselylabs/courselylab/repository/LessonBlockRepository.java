package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.LessonBlockEntity;

@Repository
public interface LessonBlockRepository extends JpaRepository<LessonBlockEntity, UUID> {

    List<LessonBlockEntity> findByLessonIdOrderByPositionAsc(UUID lessonId);

    long countByLessonIdAndType(UUID lessonId, String type);

    void deleteByLessonId(UUID lessonId);
}
