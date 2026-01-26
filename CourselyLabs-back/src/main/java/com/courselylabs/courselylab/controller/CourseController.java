package com.courselylabs.courselylab.controller;

import com.courselylabs.courselylab.dto.CourseDTO;
import com.courselylabs.courselylab.service.CourseService;
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
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<Page<CourseDTO>> findAllPublished(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(courseService.findAllPublished(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CourseDTO>> findAll() {
        return ResponseEntity.ok(courseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CourseDTO> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.findBySlug(slug));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<CourseDTO>> findByCategory(
            @PathVariable Integer categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(courseService.findByCategory(categoryId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CourseDTO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(courseService.search(keyword, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CourseDTO>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(courseService.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<CourseDTO> create(@Valid @RequestBody CourseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> update(@PathVariable UUID id, @Valid @RequestBody CourseDTO dto) {
        return ResponseEntity.ok(courseService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<CourseDTO> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.publish(id));
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<CourseDTO> unpublish(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.unpublish(id));
    }

    @PatchMapping("/{id}/submit-review")
    public ResponseEntity<CourseDTO> submitForReview(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.submitForReview(id));
    }
}
