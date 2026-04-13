package com.courselylabs.courselylab.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.BlockedPrerequisiteDTO;
import com.courselylabs.courselylab.dto.CoursePrerequisiteDTO;
import com.courselylabs.courselylab.dto.CreateCoursePrerequisiteRequestDTO;
import com.courselylabs.courselylab.security.UserDetailsImpl;
import com.courselylabs.courselylab.service.CoursePrerequisiteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses")
public class CoursePrerequisiteController {

    private final CoursePrerequisiteService prerequisiteService;

    public CoursePrerequisiteController(CoursePrerequisiteService prerequisiteService) {
        this.prerequisiteService = prerequisiteService;
    }

    // Solo premium o admin
    @GetMapping("/{id}/prerequisites")
    public ResponseEntity<List<CoursePrerequisiteDTO>> findByCourseId(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(prerequisiteService.findByCourseId(id, currentUser.getUsername()));
    }

    // Solo premium o admin
    @GetMapping("/{id}/prerequisites/blockers")
    public ResponseEntity<List<BlockedPrerequisiteDTO>> findBlockedPrerequisites(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(prerequisiteService.findBlockedPrerequisites(id, currentUser.getUsername()));
    }

    // Solo instructor del curso o admin
    @PostMapping("/{id}/prerequisites")
    public ResponseEntity<CoursePrerequisiteDTO> addPrerequisite(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCoursePrerequisiteRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prerequisiteService.addPrerequisite(id, request, currentUser.getUsername()));
    }

    // Solo instructor del curso o admin
    @DeleteMapping("/{id}/prerequisites/{prereqId}")
    public ResponseEntity<Void> deletePrerequisite(
            @PathVariable UUID id,
            @PathVariable UUID prereqId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        prerequisiteService.deletePrerequisite(id, prereqId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}