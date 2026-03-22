package com.courselylabs.courselylab.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.ReorderRequestDTO;
import com.courselylabs.courselylab.dto.SectionDTO;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.SectionEntity;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.mapper.SectionMapper;
import com.courselylabs.courselylab.repository.CourseRepository;
import com.courselylabs.courselylab.repository.SectionRepository;

@Service
@Transactional
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final SectionMapper sectionMapper;

    public SectionService(SectionRepository sectionRepository,
                          CourseRepository courseRepository,
                          SectionMapper sectionMapper) {
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
        this.sectionMapper = sectionMapper;
    }

    @Transactional(readOnly = true)
    public List<SectionDTO> findByCourseId(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        return sectionMapper.toDTOList(
                sectionRepository.findByCourseIdOrderByPositionAsc(courseId));
    }

    @Transactional(readOnly = true)
    public SectionDTO findById(UUID id) {
        SectionEntity entity = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
        return sectionMapper.toDTO(entity);
    }

    public SectionDTO create(UUID courseId, SectionDTO dto) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        SectionEntity entity = new SectionEntity();
        entity.setCourse(course);
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());

        if (dto.getPosition() != null) {
            entity.setPosition(dto.getPosition());
        } else {
            entity.setPosition(sectionRepository.countByCourseId(courseId));
        }

        entity = sectionRepository.save(entity);
        return sectionMapper.toDTO(entity);
    }

    public SectionDTO update(UUID id, SectionDTO dto) {
        SectionEntity entity = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));

        sectionMapper.updateEntityFromDTO(dto, entity);
        entity = sectionRepository.save(entity);
        return sectionMapper.toDTO(entity);
    }

    public void delete(UUID id) {
        if (!sectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Section", "id", id);
        }
        sectionRepository.deleteById(id);
    }

    public void reorder(ReorderRequestDTO request) {
        for (ReorderRequestDTO.ReorderItem item : request.getItems()) {
            SectionEntity entity = sectionRepository.findById(item.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section", "id", item.getId()));
            entity.setPosition(item.getPosition());
            sectionRepository.save(entity);
        }
    }
}
