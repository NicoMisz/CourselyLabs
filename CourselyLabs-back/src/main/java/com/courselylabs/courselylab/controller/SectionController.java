package com.courselylabs.courselylab.controller;

import java.util.List;
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

import com.courselylabs.courselylab.dto.ReorderRequestDTO;
import com.courselylabs.courselylab.dto.SectionDTO;
import com.courselylabs.courselylab.service.SectionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<List<SectionDTO>> findByCourseId(@PathVariable UUID courseId) {
        return ResponseEntity.ok(sectionService.findByCourseId(courseId));
    }

    @PostMapping("/courses/{courseId}/sections")
    @PreAuthorize("@courseSecurityService.isOwnerOrInstructorOrAdmin(#courseId, authentication)")
    public ResponseEntity<SectionDTO> create(
            @PathVariable UUID courseId,
            @Valid @RequestBody SectionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sectionService.create(courseId, dto));
    }

    @PutMapping("/sections/{id}")
    @PreAuthorize("@courseSecurityService.canEditSection(#id, authentication)")
    public ResponseEntity<SectionDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody SectionDTO dto) {
        return ResponseEntity.ok(sectionService.update(id, dto));
    }

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("@courseSecurityService.canEditSection(#id, authentication)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/sections/reorder")
    public ResponseEntity<Void> reorder(@Valid @RequestBody ReorderRequestDTO request) {
        sectionService.reorder(request);
        return ResponseEntity.noContent().build();
    }
}
