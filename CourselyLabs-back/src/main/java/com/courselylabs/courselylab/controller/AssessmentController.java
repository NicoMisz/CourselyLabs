package com.courselylabs.courselylab.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.AssessmentDTO;
import com.courselylabs.courselylab.dto.QuizQuestionDTO;
import com.courselylabs.courselylab.service.AssessmentService;

@RestController
@RequestMapping("/api")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * Public detail of an assessment for a lesson — never includes correct answers.
     */
    @GetMapping("/lessons/{lessonId}/assessment")
    @PreAuthorize("@courseSecurityService.canAccessLesson(#lessonId, authentication)")
    public ResponseEntity<AssessmentDTO> findByLesson(@PathVariable UUID lessonId) {
        AssessmentDTO dto = assessmentService.findByLessonId(lessonId, false);
        return dto == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(dto);
    }

    /**
     * Detail with answers — only for instructors/admins editing the assessment.
     */
    @GetMapping("/lessons/{lessonId}/assessment/edit")
    @PreAuthorize("@courseSecurityService.canEditAssessmentByLesson(#lessonId, authentication)")
    public ResponseEntity<AssessmentDTO> findByLessonForEdit(@PathVariable UUID lessonId) {
        AssessmentDTO dto = assessmentService.findByLessonId(lessonId, true);
        return dto == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(dto);
    }

    @PostMapping("/lessons/{lessonId}/assessment")
    @PreAuthorize("@courseSecurityService.canEditAssessmentByLesson(#lessonId, authentication)")
    public ResponseEntity<AssessmentDTO> create(
            @PathVariable UUID lessonId,
            @RequestBody AssessmentDTO dto,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assessmentService.createForLesson(lessonId, dto, auth.getName()));
    }

    @PutMapping("/assessments/{id}")
    @PreAuthorize("@courseSecurityService.canEditAssessment(#id, authentication)")
    public ResponseEntity<AssessmentDTO> update(@PathVariable UUID id, @RequestBody AssessmentDTO dto) {
        return ResponseEntity.ok(assessmentService.update(id, dto));
    }

    @DeleteMapping("/assessments/{id}")
    @PreAuthorize("@courseSecurityService.canEditAssessment(#id, authentication)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assessmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assessments/{id}/questions")
    @PreAuthorize("@courseSecurityService.canEditAssessment(#id, authentication)")
    public ResponseEntity<QuizQuestionDTO> addQuestion(
            @PathVariable UUID id,
            @RequestBody QuizQuestionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assessmentService.addQuestion(id, dto));
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<QuizQuestionDTO> updateQuestion(
            @PathVariable UUID questionId,
            @RequestBody QuizQuestionDTO dto) {
        // Permission check is done indirectly: only existing questions of editable assessments matter.
        // For simplicity, we check at service level.
        return ResponseEntity.ok(assessmentService.updateQuestion(questionId, dto));
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID questionId) {
        assessmentService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
}
