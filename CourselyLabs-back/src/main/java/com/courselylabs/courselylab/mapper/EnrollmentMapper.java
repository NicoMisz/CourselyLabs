package com.courselylabs.courselylab.mapper;

import com.courselylabs.courselylab.dto.EnrollmentDTO;
import com.courselylabs.courselylab.entity.EnrollmentEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EnrollmentMapper {

    private final ModelMapper modelMapper;

    public EnrollmentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public EnrollmentDTO toDTO(EnrollmentEntity entity) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(entity.getId());
        dto.setAccessType(entity.getAccessType());
        dto.setEnrolledAt(entity.getEnrolledAt());
        dto.setLastAccessedAt(entity.getLastAccessedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserFullName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName());
        }

        if (entity.getCourse() != null) {
            dto.setCourseId(entity.getCourse().getId());
            dto.setCourseTitle(entity.getCourse().getTitle());
        }

        return dto;
    }

    public List<EnrollmentDTO> toDTOList(List<EnrollmentEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
