package com.courselylabs.courselylab.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.LessonBlockDTO;
import com.courselylabs.courselylab.entity.AssessmentEntity;
import com.courselylabs.courselylab.entity.LessonBlockEntity;
import com.courselylabs.courselylab.entity.LessonEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.repository.AssessmentRepository;
import com.courselylabs.courselylab.repository.LessonBlockRepository;
import com.courselylabs.courselylab.repository.LessonRepository;

@Service
@Transactional
public class LessonBlockService {

    private static final Set<String> VALID_TYPES = Set.of(
            "text", "video", "pdf", "quiz", "project", "open_text"
    );

    private final LessonBlockRepository blockRepository;
    private final LessonRepository lessonRepository;
    private final AssessmentRepository assessmentRepository;

    public LessonBlockService(LessonBlockRepository blockRepository,
                              LessonRepository lessonRepository,
                              AssessmentRepository assessmentRepository) {
        this.blockRepository = blockRepository;
        this.lessonRepository = lessonRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @Transactional(readOnly = true)
    public List<LessonBlockDTO> findByLessonId(UUID lessonId) {
        return blockRepository.findByLessonIdOrderByPositionAsc(lessonId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public LessonBlockDTO create(UUID lessonId, LessonBlockDTO dto) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        if (!VALID_TYPES.contains(dto.getType())) {
            throw new BadRequestException("Tipo de bloque invalido: " + dto.getType());
        }

        int position = dto.getPosition() != null
                ? dto.getPosition()
                : (int) blockRepository.findByLessonIdOrderByPositionAsc(lessonId).size();

        LessonBlockEntity entity = new LessonBlockEntity();
        entity.setLesson(lesson);
        entity.setType(dto.getType());
        entity.setPosition(position);
        entity.setTextContent(dto.getTextContent());
        entity.setVideoUrl(dto.getVideoUrl());
        entity.setPdfUrl(dto.getPdfUrl());
        entity.setLabProvider(dto.getLabProvider());
        entity.setLabTemplateId(dto.getLabTemplateId());
        entity.setLabInstructions(dto.getLabInstructions());

        return toDTO(blockRepository.save(entity));
    }

    public LessonBlockDTO update(UUID blockId, LessonBlockDTO dto) {
        LessonBlockEntity entity = blockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", blockId));

        if (dto.getTextContent() != null) entity.setTextContent(dto.getTextContent());
        if (dto.getVideoUrl() != null) entity.setVideoUrl(dto.getVideoUrl());
        if (dto.getPdfUrl() != null) entity.setPdfUrl(dto.getPdfUrl());
        if (dto.getPosition() != null) entity.setPosition(dto.getPosition());
        if (dto.getLabProvider() != null) entity.setLabProvider(dto.getLabProvider());
        if (dto.getLabTemplateId() != null) entity.setLabTemplateId(dto.getLabTemplateId());
        if (dto.getLabInstructions() != null) entity.setLabInstructions(dto.getLabInstructions());

        return toDTO(blockRepository.save(entity));
    }

    public void delete(UUID blockId) {
        if (!blockRepository.existsById(blockId)) {
            throw new ResourceNotFoundException("Block", "id", blockId);
        }
        blockRepository.deleteById(blockId);
    }

    public void reorder(UUID lessonId, List<UUID> orderedIds) {
        List<LessonBlockEntity> blocks = blockRepository.findByLessonIdOrderByPositionAsc(lessonId);
        for (LessonBlockEntity block : blocks) {
            int idx = orderedIds.indexOf(block.getId());
            if (idx >= 0) {
                block.setPosition(idx);
            }
        }
        blockRepository.saveAll(blocks);
    }

    public LessonBlockDTO toDTO(LessonBlockEntity entity) {
        LessonBlockDTO dto = new LessonBlockDTO();
        dto.setId(entity.getId());
        dto.setLessonId(entity.getLesson().getId());
        dto.setType(entity.getType());
        dto.setPosition(entity.getPosition());
        dto.setTextContent(entity.getTextContent());
        dto.setVideoUrl(entity.getVideoUrl());
        dto.setPdfUrl(entity.getPdfUrl());
        dto.setLabProvider(entity.getLabProvider());
        dto.setLabTemplateId(entity.getLabTemplateId());
        dto.setLabInstructions(entity.getLabInstructions());

        // Link assessment if exists
        if (isAssessmentType(entity.getType())) {
            Optional<AssessmentEntity> assessment = assessmentRepository.findByBlockId(entity.getId());
            assessment.ifPresent(a -> dto.setAssessmentId(a.getId()));
        }

        return dto;
    }

    public static boolean isAssessmentType(String type) {
        return "quiz".equals(type) || "project".equals(type) || "open_text".equals(type);
    }
}
