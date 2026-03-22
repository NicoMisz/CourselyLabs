package com.courselylabs.courselylab.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.LessonDTO;
import com.courselylabs.courselylab.dto.ReorderRequestDTO;
import com.courselylabs.courselylab.entity.LessonEntity;
import com.courselylabs.courselylab.entity.SectionEntity;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.mapper.LessonMapper;
import com.courselylabs.courselylab.repository.LessonRepository;
import com.courselylabs.courselylab.repository.SectionRepository;

@Service
@Transactional
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final LessonMapper lessonMapper;

    public LessonService(LessonRepository lessonRepository,
                         SectionRepository sectionRepository,
                         LessonMapper lessonMapper) {
        this.lessonRepository = lessonRepository;
        this.sectionRepository = sectionRepository;
        this.lessonMapper = lessonMapper;
    }

    @Transactional(readOnly = true)
    public List<LessonDTO> findBySectionId(UUID sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section", "id", sectionId);
        }
        return lessonMapper.toDTOList(
                lessonRepository.findBySectionIdOrderByPositionAsc(sectionId));
    }

    @Transactional(readOnly = true)
    public LessonDTO findById(UUID id) {
        LessonEntity entity = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", id));
        return lessonMapper.toDTO(entity);
    }

    public LessonDTO create(UUID sectionId, LessonDTO dto) {
        SectionEntity section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        LessonEntity entity = new LessonEntity();
        entity.setSection(section);
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setContentUrl(dto.getContentUrl());
        entity.setContentText(dto.getContentText());
        entity.setDuration(dto.getDuration());
        entity.setIsFree(dto.getIsFree() != null ? dto.getIsFree() : false);

        if (dto.getPosition() != null) {
            entity.setPosition(dto.getPosition());
        } else {
            entity.setPosition(lessonRepository.countBySectionId(sectionId));
        }

        entity = lessonRepository.save(entity);
        return lessonMapper.toDTO(entity);
    }

    public LessonDTO update(UUID id, LessonDTO dto) {
        LessonEntity entity = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", id));

        lessonMapper.updateEntityFromDTO(dto, entity);
        entity = lessonRepository.save(entity);
        return lessonMapper.toDTO(entity);
    }

    public void delete(UUID id) {
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson", "id", id);
        }
        lessonRepository.deleteById(id);
    }

    public void reorder(ReorderRequestDTO request) {
        for (ReorderRequestDTO.ReorderItem item : request.getItems()) {
            LessonEntity entity = lessonRepository.findById(item.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", item.getId()));
            entity.setPosition(item.getPosition());
            lessonRepository.save(entity);
        }
    }
}
