package com.courselylabs.courselylab.mapper;

import com.courselylabs.courselylab.dto.CategoriaDTO;
import com.courselylabs.courselylab.entity.CategoriaEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoriaMapper {

    private final ModelMapper modelMapper;

    public CategoriaMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CategoriaDTO toDTO(CategoriaEntity entity) {
        return modelMapper.map(entity, CategoriaDTO.class);
    }

    public CategoriaEntity toEntity(CategoriaDTO dto) {
        return modelMapper.map(dto, CategoriaEntity.class);
    }

    public List<CategoriaDTO> toDTOList(List<CategoriaEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(CategoriaDTO dto, CategoriaEntity entity) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getSlug() != null) {
            entity.setSlug(dto.getSlug());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
    }
}
