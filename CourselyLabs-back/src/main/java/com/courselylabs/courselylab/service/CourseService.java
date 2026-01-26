package com.courselylabs.courselylab.service;

import com.courselylabs.courselylab.dto.CourseDTO;
import com.courselylabs.courselylab.entity.CategoriaEntity;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.mapper.CourseMapper;
import com.courselylabs.courselylab.repository.CategoriaRepository;
import com.courselylabs.courselylab.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoriaRepository categoriaRepository;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepository, CategoriaRepository categoriaRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.categoriaRepository = categoriaRepository;
        this.courseMapper = courseMapper;
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> findAll() {
        return courseMapper.toDTOList(courseRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> findAllPublished(Pageable pageable) {
        return courseRepository.findByIsPublishedTrue(pageable)
                .map(courseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public CourseDTO findById(UUID id) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return courseMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public CourseDTO findBySlug(String slug) {
        CourseEntity entity = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "slug", slug));
        return courseMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> findByCategory(Integer categoryId, Pageable pageable) {
        return courseRepository.findByCategoryIdAndIsPublishedTrue(categoryId, pageable)
                .map(courseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> search(String keyword, Pageable pageable) {
        return courseRepository.searchPublishedCourses(keyword, pageable)
                .map(courseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> findByStatus(String status) {
        return courseMapper.toDTOList(courseRepository.findByStatus(status));
    }

    public CourseDTO create(CourseDTO dto) {
        if (courseRepository.existsBySlug(dto.getSlug())) {
            throw new BadRequestException("Course with slug '" + dto.getSlug() + "' already exists");
        }

        CourseEntity entity = courseMapper.toEntity(dto);

        if (dto.getCategoryId() != null) {
            CategoriaEntity category = categoriaRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            entity.setCategory(category);
        }

        entity.setStatus("draft");
        entity.setIsPublished(false);
        entity = courseRepository.save(entity);
        return courseMapper.toDTO(entity);
    }

    public CourseDTO update(UUID id, CourseDTO dto) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        if (dto.getSlug() != null && !dto.getSlug().equals(entity.getSlug())) {
            if (courseRepository.existsBySlug(dto.getSlug())) {
                throw new BadRequestException("Course with slug '" + dto.getSlug() + "' already exists");
            }
        }

        if (dto.getCategoryId() != null) {
            CategoriaEntity category = categoriaRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            entity.setCategory(category);
        }

        courseMapper.updateEntityFromDTO(dto, entity);
        entity = courseRepository.save(entity);
        return courseMapper.toDTO(entity);
    }

    public CourseDTO publish(UUID id) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        entity.setIsPublished(true);
        entity.setStatus("published");
        entity.setPublishedAt(LocalDateTime.now());
        entity = courseRepository.save(entity);
        return courseMapper.toDTO(entity);
    }

    public CourseDTO unpublish(UUID id) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        entity.setIsPublished(false);
        entity.setStatus("draft");
        entity = courseRepository.save(entity);
        return courseMapper.toDTO(entity);
    }

    public CourseDTO submitForReview(UUID id) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        entity.setStatus("pending_review");
        entity = courseRepository.save(entity);
        return courseMapper.toDTO(entity);
    }

    public void delete(UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", "id", id);
        }
        courseRepository.deleteById(id);
    }
}
