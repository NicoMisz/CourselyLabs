package com.courselylabs.courselylab.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.CourseProgressDTO;
import com.courselylabs.courselylab.dto.LessonProgressDTO;
import com.courselylabs.courselylab.dto.PositionUpdateDTO;
import com.courselylabs.courselylab.service.LessonProgressService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/progress")
public class LessonProgressController {

    private final LessonProgressService progressService;

    public LessonProgressController(LessonProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<LessonProgressDTO> toggleComplete(
            @PathVariable UUID lessonId,
            Authentication auth) {
        return ResponseEntity.ok(progressService.toggleComplete(auth.getName(), lessonId));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseProgressDTO> getCourseProgress(
            @PathVariable UUID courseId,
            Authentication auth) {
        return ResponseEntity.ok(progressService.getCourseProgress(auth.getName(), courseId));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonProgressDTO> getLessonProgress(
            @PathVariable UUID lessonId,
            Authentication auth) {
        return ResponseEntity.ok(progressService.getLessonProgress(auth.getName(), lessonId));
    }

    @PatchMapping("/lessons/{lessonId}/position")
    public ResponseEntity<LessonProgressDTO> updatePosition(
            @PathVariable UUID lessonId,
            @Valid @RequestBody PositionUpdateDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(
                progressService.updatePosition(auth.getName(), lessonId, dto.getPositionSeconds()));
    }
}
