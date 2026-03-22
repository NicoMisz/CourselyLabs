package com.courselylabs.courselylab.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.courselylabs.courselylab.dto.SectionDTO;
import com.courselylabs.courselylab.entity.SectionEntity;

@Component
public class SectionMapper {

    private final LessonMapper lessonMapper;

    public SectionMapper(LessonMapper lessonMapper) {
        this.lessonMapper = lessonMapper;
    }

    public SectionDTO toDTO(SectionEntity entity) {
        SectionDTO dto = new SectionDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setPosition(entity.getPosition());
        dto.setCourseId(entity.getCourse().getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getLessons() != null) {
            dto.setLessons(lessonMapper.toDTOList(entity.getLessons()));
        }

        return dto;
    }

    public List<SectionDTO> toDTOList(List<SectionEntity> entities) {
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void updateEntityFromDTO(SectionDTO dto, SectionEntity entity) {
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getPosition() != null) entity.setPosition(dto.getPosition());
    }
}
