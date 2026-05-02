package com.courselylabs.courselylab.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.access.prepost.PreAuthorize;

import com.courselylabs.courselylab.dto.LessonDTO;
import com.courselylabs.courselylab.dto.ReorderRequestDTO;
import com.courselylabs.courselylab.service.LessonService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/lessons/{id}")
    @PreAuthorize("@courseSecurityService.canAccessLesson(#id, authentication)")
    public ResponseEntity<LessonDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(lessonService.findById(id));
    }

    @PostMapping("/sections/{sectionId}/lessons")
    @PreAuthorize("@courseSecurityService.canEditSection(#sectionId, authentication)")
    public ResponseEntity<LessonDTO> create(
            @PathVariable UUID sectionId,
            @Valid @RequestBody LessonDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lessonService.create(sectionId, dto));
    }

    @PutMapping("/lessons/{id}")
    @PreAuthorize("@courseSecurityService.canEditLesson(#id, authentication)")
    public ResponseEntity<LessonDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody LessonDTO dto) {
        return ResponseEntity.ok(lessonService.update(id, dto));
    }

    @DeleteMapping("/lessons/{id}")
    @PreAuthorize("@courseSecurityService.canEditLesson(#id, authentication)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/lessons/reorder")
    public ResponseEntity<Void> reorder(@Valid @RequestBody ReorderRequestDTO request) {
        lessonService.reorder(request);
        return ResponseEntity.noContent().build();
    }
}
