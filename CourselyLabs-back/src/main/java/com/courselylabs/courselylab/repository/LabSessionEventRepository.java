package com.courselylabs.courselylab.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.LabSessionEventEntity;

@Repository
public interface LabSessionEventRepository extends JpaRepository<LabSessionEventEntity, UUID> {

    List<LabSessionEventEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<LabSessionEventEntity> findByBlockIdOrderByCreatedAtDesc(UUID blockId);
}
