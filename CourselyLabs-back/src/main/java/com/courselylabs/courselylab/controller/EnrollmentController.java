package com.courselylabs.courselylab.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.EnrollRequestDTO;
import com.courselylabs.courselylab.dto.EnrolledCourseDTO;
import com.courselylabs.courselylab.dto.EnrollmentDTO;
import com.courselylabs.courselylab.security.UserDetailsImpl;
import com.courselylabs.courselylab.service.EnrollmentService;

import jakarta.validation.Valid;

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

    @GetMapping("/check")
    public ResponseEntity<Boolean> isCurrentUserEnrolled(
        @AuthenticationPrincipal UserDetailsImpl currentUser,
        @RequestParam UUID courseId
    ) {
        return ResponseEntity.ok(
            enrollmentService.isCurrentUserEnrolled(currentUser.getUsername(), courseId)
        );
    }

    @GetMapping("/me/courses")
    public ResponseEntity<List<EnrolledCourseDTO>> findMyCourses(
        @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return ResponseEntity.ok(enrollmentService.findMyCourses(currentUser.getUsername()));
    }

    @PostMapping
    public ResponseEntity<EnrollmentDTO> enroll(@Valid @RequestBody EnrollmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(dto));
    }

    @PostMapping("/me")
    public ResponseEntity<EnrollmentDTO> enrollCurrentUser(
        @AuthenticationPrincipal UserDetailsImpl currentUser,
        @Valid @RequestBody EnrollRequestDTO request
    ) {
        EnrollmentDTO dto = enrollmentService.enrollCurrentUser(currentUser.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PatchMapping("/{id}/access")
    public ResponseEntity<EnrollmentDTO> updateLastAccessed(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.updateLastAccessed(id));
    }

    @PatchMapping("/me/access")
    public ResponseEntity<Void> updateMyLastAccess(
        @AuthenticationPrincipal UserDetailsImpl currentUser,
        @RequestParam UUID courseId
    ) {
    enrollmentService.updateLastAccessForCurrentUser(currentUser.getUsername(), courseId);
    return ResponseEntity.noContent().build();
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
