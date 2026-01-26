package com.courselylabs.courselylab.mapper;

import com.courselylabs.courselylab.dto.CourseDTO;
import com.courselylabs.courselylab.entity.CourseEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseMapper {

    private final ModelMapper modelMapper;

    public CourseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CourseDTO toDTO(CourseEntity entity) {
        CourseDTO dto = modelMapper.map(entity, CourseDTO.class);
        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
            dto.setCategoryName(entity.getCategory().getName());
        }
        return dto;
    }

    public CourseEntity toEntity(CourseDTO dto) {
        return modelMapper.map(dto, CourseEntity.class);
    }

    public List<CourseDTO> toDTOList(List<CourseEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(CourseDTO dto, CourseEntity entity) {
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getSlug() != null) {
            entity.setSlug(dto.getSlug());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getShortDescription() != null) {
            entity.setShortDescription(dto.getShortDescription());
        }
        if (dto.getThumbnailUrl() != null) {
            entity.setThumbnailUrl(dto.getThumbnailUrl());
        }
        if (dto.getLevel() != null) {
            entity.setLevel(dto.getLevel());
        }
        if (dto.getIsFree() != null) {
            entity.setIsFree(dto.getIsFree());
        }
        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }
}
