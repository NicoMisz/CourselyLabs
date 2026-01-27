package com.courselylabs.courselylab.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.courselylabs.courselylab.dto.EnrollmentDTO;
import com.courselylabs.courselylab.entity.EnrollmentEntity;

@Component
public class EnrollmentMapper {

    private final ModelMapper modelMapper;

    //TODO quiero entender
    public EnrollmentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public EnrollmentEntity toEntity (EnrollmentDTO dto) {
        EnrollmentEntity entity = new EnrollmentEntity();
        entity.setId(dto.getId());
        entity.setAccessType(dto.getAccessType());
        entity.setEnrolledAt(dto.getEnrolledAt());
        entity.setLastAccessedAt(dto.getLastAccessedAt());
        // Note: User and Course entities should be set separately in the service layer
        return entity;
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
