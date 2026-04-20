package com.courselylabs.courselylab.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.courselylabs.courselylab.dto.LessonResourceDTO;
import com.courselylabs.courselylab.service.LessonResourceService;

@RestController
@RequestMapping("/api")
public class LessonResourceController {

    private final LessonResourceService resourceService;

    public LessonResourceController(LessonResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/lessons/{lessonId}/resources")
    @PreAuthorize("@courseSecurityService.canAccessLesson(#lessonId, authentication)")
    public ResponseEntity<List<LessonResourceDTO>> list(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(resourceService.findByLessonId(lessonId));
    }

    @PostMapping("/lessons/{lessonId}/resources")
    @PreAuthorize("@courseSecurityService.canEditLesson(#lessonId, authentication)")
    public ResponseEntity<LessonResourceDTO> upload(
            @PathVariable UUID lessonId,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.uploadResource(lessonId, file, auth.getName()));
    }

    @GetMapping("/resources/{id}/download")
    @PreAuthorize("@courseSecurityService.canAccessResource(#id, authentication)")
    public ResponseEntity<Map<String, String>> download(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("url", resourceService.getDownloadUrl(id)));
    }

    @DeleteMapping("/resources/{id}")
    @PreAuthorize("@courseSecurityService.canEditResource(#id, authentication)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    // --- Generic uploads (thumbnails, lesson content) ---

    @PostMapping("/courses/{courseId}/thumbnail")
    @PreAuthorize("@courseSecurityService.isOwnerOrInstructorOrAdmin(#courseId, authentication)")
    public ResponseEntity<Map<String, String>> uploadThumbnail(
            @PathVariable UUID courseId,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        return ResponseEntity.ok(Map.of("url", resourceService.uploadThumbnail(courseId, file, auth.getName())));
    }

    @PostMapping("/lessons/{lessonId}/content-upload")
    @PreAuthorize("@courseSecurityService.canEditLesson(#lessonId, authentication)")
    public ResponseEntity<Map<String, String>> uploadLessonContent(
            @PathVariable UUID lessonId,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        return ResponseEntity.ok(Map.of("url", resourceService.uploadLessonContent(lessonId, file, auth.getName())));
    }
}
