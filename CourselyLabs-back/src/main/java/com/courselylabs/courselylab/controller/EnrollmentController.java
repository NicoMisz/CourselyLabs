package com.courselylabs.courselylab.controller;

import com.courselylabs.courselylab.dto.EnrollmentDTO;
import com.courselylabs.courselylab.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EnrollmentDTO>> findByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(enrollmentService.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/paged")
    public ResponseEntity<Page<EnrollmentDTO>> findByUserIdPaged(
            @PathVariable UUID userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.findByUserId(userId, pageable));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<EnrollmentDTO>> findByCourseId(
            @PathVariable UUID courseId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.findByCourseId(courseId, pageable));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isEnrolled(
            @RequestParam UUID userId,
            @RequestParam UUID courseId) {
        return ResponseEntity.ok(enrollmentService.isEnrolled(userId, courseId));
    }

    @PostMapping
    public ResponseEntity<EnrollmentDTO> enroll(@Valid @RequestBody EnrollmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(dto));
    }

    @PatchMapping("/{id}/access")
    public ResponseEntity<EnrollmentDTO> updateLastAccessed(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.updateLastAccessed(id));
    }

    @DeleteMapping
    public ResponseEntity<Void> unenroll(
            @RequestParam UUID userId,
            @RequestParam UUID courseId) {
        enrollmentService.unenroll(userId, courseId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
