package com.courselylabs.courselylab.service;

import com.courselylabs.courselylab.dto.CategoriaDTO;
import com.courselylabs.courselylab.entity.CategoriaEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.mapper.CategoriaMapper;
import com.courselylabs.courselylab.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> findAll() {
        return categoriaMapper.toDTOList(categoriaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CategoriaDTO findById(Integer id) {
        CategoriaEntity entity = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return categoriaMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public CategoriaDTO findBySlug(String slug) {
        CategoriaEntity entity = categoriaRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return categoriaMapper.toDTO(entity);
    }

    public CategoriaDTO create(CategoriaDTO dto) {
        if (categoriaRepository.existsBySlug(dto.getSlug())) {
            throw new BadRequestException("Category with slug '" + dto.getSlug() + "' already exists");
        }
        if (categoriaRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Category with name '" + dto.getName() + "' already exists");
        }

        CategoriaEntity entity = categoriaMapper.toEntity(dto);
        entity = categoriaRepository.save(entity);
        return categoriaMapper.toDTO(entity);
    }

    public CategoriaDTO update(Integer id, CategoriaDTO dto) {
        CategoriaEntity entity = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (dto.getSlug() != null && !dto.getSlug().equals(entity.getSlug())) {
            if (categoriaRepository.existsBySlug(dto.getSlug())) {
                throw new BadRequestException("Category with slug '" + dto.getSlug() + "' already exists");
            }
        }

        if (dto.getName() != null && !dto.getName().equals(entity.getName())) {
            if (categoriaRepository.existsByName(dto.getName())) {
                throw new BadRequestException("Category with name '" + dto.getName() + "' already exists");
            }
        }

        categoriaMapper.updateEntityFromDTO(dto, entity);
        entity = categoriaRepository.save(entity);
        return categoriaMapper.toDTO(entity);
    }

    public void delete(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        categoriaRepository.deleteById(id);
    }
}
