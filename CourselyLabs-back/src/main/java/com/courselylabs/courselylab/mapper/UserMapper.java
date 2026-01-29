package com.courselylabs.courselylab.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.courselylabs.courselylab.dto.UserCreateDTO;
import com.courselylabs.courselylab.dto.UserDTO;
import com.courselylabs.courselylab.entity.UserEntity;

@Component
public class UserMapper {

    private final ModelMapper modelMapper;

    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public UserDTO toDTO(UserEntity entity) {
        return modelMapper.map(entity, UserDTO.class);
    }

    public UserEntity toEntity(UserDTO dto) {
        return modelMapper.map(dto, UserEntity.class);
    }

    public UserEntity toEntity(UserCreateDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setEmail(dto.getEmail());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setRole(dto.getRole() != null ? dto.getRole() : "student");
        entity.setBio(dto.getBio());
        entity.setProfilePictureUrl(dto.getProfilePictureUrl());
        return entity;
    }

    
    public List<UserDTO> toDTOList(List<UserEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    
    public void updateEntityFromDTO(UserDTO dto, UserEntity entity) {
        if (dto.getFirstName() != null) {
            entity.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            entity.setLastName(dto.getLastName());
        }
        if (dto.getBio() != null) {
            entity.setBio(dto.getBio());
        }
        if (dto.getProfilePictureUrl() != null) {
            entity.setProfilePictureUrl(dto.getProfilePictureUrl());
        }
        if (dto.getRole() != null) {
            entity.setRole(dto.getRole());
        }
    }
}
