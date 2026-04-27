package com.courselylabs.courselylab.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.courselylabs.courselylab.dto.LessonDTO;
import com.courselylabs.courselylab.entity.LessonEntity;
import com.courselylabs.courselylab.service.LessonBlockService;

@Component
public class LessonMapper {

    private final LessonBlockService blockService;

    public LessonMapper(LessonBlockService blockService) {
        this.blockService = blockService;
    }

    public LessonDTO toDTO(LessonEntity entity) {
        LessonDTO dto = new LessonDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setType(entity.getType());
        dto.setContentUrl(entity.getContentUrl());
        dto.setContentText(entity.getContentText());
        dto.setDuration(entity.getDuration());
        dto.setPosition(entity.getPosition());
        dto.setIsFree(entity.getIsFree());
        dto.setSectionId(entity.getSection().getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setBlocks(blockService.findByLessonId(entity.getId()));
        return dto;
    }

    public List<LessonDTO> toDTOList(List<LessonEntity> entities) {
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void updateEntityFromDTO(LessonDTO dto, LessonEntity entity) {
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getType() != null) entity.setType(dto.getType());
        if (dto.getContentUrl() != null) entity.setContentUrl(dto.getContentUrl());
        if (dto.getContentText() != null) entity.setContentText(dto.getContentText());
        if (dto.getDuration() != null) entity.setDuration(dto.getDuration());
        if (dto.getPosition() != null) entity.setPosition(dto.getPosition());
        if (dto.getIsFree() != null) entity.setIsFree(dto.getIsFree());
    }
}
