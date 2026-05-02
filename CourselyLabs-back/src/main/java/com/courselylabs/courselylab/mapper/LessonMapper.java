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

    /**
     * DTO completo de la lección, incluyendo bloques y contenido legacy.
     * Usar SOLO en endpoints protegidos por canAccessLesson (vista de
     * la lección reproduciendo su contenido).
     */
    public LessonDTO toDTO(LessonEntity entity) {
        return toDTO(entity, true);
    }

    /**
     * Si {@code includeContent} es false, devuelve metadatos públicos
     * (id, title, description, duration, position, isFree, sectionId,
     * timestamps) sin bloques ni texto/URL del contenido.
     * Esto evita exponer el contenido a usuarios no inscritos cuando
     * solo necesitan el listado del curso (sidebar/preview).
     */
    public LessonDTO toDTO(LessonEntity entity, boolean includeContent) {
        LessonDTO dto = new LessonDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setType(entity.getType());
        dto.setDuration(entity.getDuration());
        dto.setPosition(entity.getPosition());
        dto.setIsFree(entity.getIsFree());
        dto.setSectionId(entity.getSection().getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (includeContent) {
            dto.setContentUrl(entity.getContentUrl());
            dto.setContentText(entity.getContentText());
            dto.setBlocks(blockService.findByLessonId(entity.getId()));
        }
        return dto;
    }

    public List<LessonDTO> toDTOList(List<LessonEntity> entities) {
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Variante para listados públicos (sidebar de curso) — sin contenido.
     */
    public List<LessonDTO> toSummaryList(List<LessonEntity> entities) {
        return entities.stream().map(e -> toDTO(e, false)).collect(Collectors.toList());
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
