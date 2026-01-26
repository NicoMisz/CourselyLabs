package com.courselylabs.courselylab.controller;

import com.courselylabs.courselylab.dto.ReviewDTO;
import com.courselylabs.courselylab.service.ReviewService;
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

    @PostMapping
    public ResponseEntity<ReviewDTO> create(@Valid @RequestBody ReviewDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> update(@PathVariable UUID id, @Valid @RequestBody ReviewDTO dto) {
        return ResponseEntity.ok(reviewService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
