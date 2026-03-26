package com.courselylabs.courselylab.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.CreateReviewRequestDTO;
import com.courselylabs.courselylab.dto.ReviewDTO;
import com.courselylabs.courselylab.dto.UpdateReviewRequestDTO;
import com.courselylabs.courselylab.service.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ReviewDTO>> findByCourseId(@PathVariable UUID courseId) {
        return ResponseEntity.ok(reviewService.findByCourseId(courseId));
    }

    @GetMapping("/course/{courseId}/paged")
    public ResponseEntity<Page<ReviewDTO>> findByCourseIdPaged(
            @PathVariable UUID courseId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(reviewService.findByCourseId(courseId, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> findByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.findByUserId(userId));
    }

    @GetMapping("/course/{courseId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID courseId) {
        return ResponseEntity.ok(reviewService.getAverageRating(courseId));
    }

    @GetMapping("/course/{courseId}/me")
    public ResponseEntity<ReviewDTO> getMyReview(@PathVariable UUID courseId, Authentication auth) {
        return ResponseEntity.ok(reviewService.getMyReview(courseId, auth.getName()));
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> create(@Valid @RequestBody ReviewDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(dto));
    }

    @PostMapping("/course/{courseId}")
    public ResponseEntity<ReviewDTO> createForCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateReviewRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createForCourse(courseId, dto, auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> update(@PathVariable UUID id, @Valid @RequestBody ReviewDTO dto) {
        return ResponseEntity.ok(reviewService.update(id, dto));
    }

    @PutMapping("/{id}/own")
    public ResponseEntity<ReviewDTO> updateOwn(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReviewRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(reviewService.updateOwn(id, dto, auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/own")
    public ResponseEntity<Void> deleteOwn(@PathVariable UUID id, Authentication auth) {
        reviewService.deleteOwn(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
