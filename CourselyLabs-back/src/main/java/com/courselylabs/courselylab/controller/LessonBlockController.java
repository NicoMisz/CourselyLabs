package com.courselylabs.courselylab.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.LessonBlockDTO;
import com.courselylabs.courselylab.service.LessonBlockService;

@RestController
@RequestMapping("/api")
public class LessonBlockController {

    private final LessonBlockService blockService;

    public LessonBlockController(LessonBlockService blockService) {
        this.blockService = blockService;
    }

    @GetMapping("/lessons/{lessonId}/blocks")
    @PreAuthorize("@courseSecurityService.canAccessLesson(#lessonId, authentication)")
    public ResponseEntity<List<LessonBlockDTO>> findByLesson(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(blockService.findByLessonId(lessonId));
    }

    @PostMapping("/lessons/{lessonId}/blocks")
    @PreAuthorize("@courseSecurityService.canEditLesson(#lessonId, authentication)")
    public ResponseEntity<LessonBlockDTO> create(
            @PathVariable UUID lessonId,
            @RequestBody LessonBlockDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(blockService.create(lessonId, dto));
    }

    @PutMapping("/blocks/{id}")
    @PreAuthorize("@courseSecurityService.canEditBlock(#id, authentication)")
    public ResponseEntity<LessonBlockDTO> update(
            @PathVariable UUID id,
            @RequestBody LessonBlockDTO dto) {
        return ResponseEntity.ok(blockService.update(id, dto));
    }

    @DeleteMapping("/blocks/{id}")
    @PreAuthorize("@courseSecurityService.canEditBlock(#id, authentication)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        blockService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/lessons/{lessonId}/blocks/reorder")
    @PreAuthorize("@courseSecurityService.canEditLesson(#lessonId, authentication)")
    public ResponseEntity<Void> reorder(
            @PathVariable UUID lessonId,
            @RequestBody Map<String, List<UUID>> body) {
        blockService.reorder(lessonId, body.getOrDefault("orderedIds", List.of()));
        return ResponseEntity.noContent().build();
    }
}
