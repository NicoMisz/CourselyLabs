package com.courselylabs.courselylab.mapper;

import com.courselylabs.courselylab.dto.ReviewDTO;
import com.courselylabs.courselylab.entity.ReviewEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    private final ModelMapper modelMapper;

    public ReviewMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ReviewDTO toDTO(ReviewEntity entity) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(entity.getId());
        dto.setRating(entity.getRating());
        dto.setComment(entity.getComment());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getCourse() != null) {
            dto.setCourseId(entity.getCourse().getId());
            dto.setCourseTitle(entity.getCourse().getTitle());
        }

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserFullName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName());
        }

        return dto;
    }

    public List<ReviewDTO> toDTOList(List<ReviewEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(ReviewDTO dto, ReviewEntity entity) {
        if (dto.getRating() != null) {
            entity.setRating(dto.getRating());
        }
        if (dto.getComment() != null) {
            entity.setComment(dto.getComment());
        }
    }
}
