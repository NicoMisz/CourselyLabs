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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.BlockedPrerequisiteDTO;
import com.courselylabs.courselylab.dto.CoursePrerequisiteDTO;
import com.courselylabs.courselylab.dto.CoursePrerequisiteStatusDTO;
import com.courselylabs.courselylab.dto.CourseRelatedResponseDTO;
import com.courselylabs.courselylab.dto.CreateCoursePrerequisiteRequestDTO;
import com.courselylabs.courselylab.dto.RelatedCourseDTO;
import com.courselylabs.courselylab.dto.SyncCoursePrerequisitesRequestDTO;
import com.courselylabs.courselylab.exception.UnauthorizedException;
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
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return ResponseEntity.ok(prerequisiteService.findByCourseId(id, currentUser.getUsername()));
    }

    // Solo premium o admin
    @GetMapping("/{id}/prerequisites/blockers")
    public ResponseEntity<List<BlockedPrerequisiteDTO>> findBlockedPrerequisites(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required");
        }

        return ResponseEntity.ok(prerequisiteService.findBlockedPrerequisites(id, currentUser.getUsername()));
    }

    // Solo premium o admin - estado completo para tab de prerequisitos
    @GetMapping("/{id}/prerequisites/status")
    public ResponseEntity<List<CoursePrerequisiteStatusDTO>> findPrerequisiteStatus(
                    @PathVariable UUID id,
                    @AuthenticationPrincipal UserDetailsImpl currentUser) {
            if (currentUser == null) {
                    throw new UnauthorizedException("Authentication required");
            }
            return ResponseEntity.ok(prerequisiteService.findPrerequisiteStatus(id, currentUser.getUsername()));
    }

    // Público: información de la ficha del curso (prerequisitos y cursos que lo requieren).
    // Útil para visitantes anónimos que quieren entender el grafo de cursos.
    @GetMapping("/{id}/related")
    public ResponseEntity<CourseRelatedResponseDTO> findRelated(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(prerequisiteService.findRelatedCourses(id, email));
    }

    // Solo instructor del curso o admin
    @PostMapping("/{id}/prerequisites")
    public ResponseEntity<CoursePrerequisiteDTO> addPrerequisite(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCoursePrerequisiteRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prerequisiteService.addPrerequisite(id, request, currentUser.getUsername()));
    }

    @PutMapping("/{id}/prerequisites")
    public ResponseEntity<Void> syncPrerequisites(
            @PathVariable UUID id,
            @Valid @RequestBody SyncCoursePrerequisitesRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (currentUser == null) {
                throw new UnauthorizedException("Authentication required");
        }
        prerequisiteService.syncPrerequisites(id, request.getPrerequisites(), currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Solo instructor del curso o admin
    @DeleteMapping("/{id}/prerequisites/{prereqId}")
    public ResponseEntity<Void> deletePrerequisite(
            @PathVariable UUID id,
            @PathVariable UUID prereqId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required");
        }
        prerequisiteService.deletePrerequisite(id, prereqId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/prerequisites/drafts")
    public ResponseEntity<List<RelatedCourseDTO>> findDraftPrerequisites(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return ResponseEntity.ok(prerequisiteService.findDraftPrerequisites(id, currentUser.getUsername()));
    }
}