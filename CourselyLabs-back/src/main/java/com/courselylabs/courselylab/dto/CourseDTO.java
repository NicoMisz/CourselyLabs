package com.courselylabs.courselylab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {

    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Slug is required")
    private String slug;

    private String description;

    @Size(max = 500, message = "Short description must be less than 500 characters")
    private String shortDescription;

    private String thumbnailUrl;

    private Integer categoryId;

    private String categoryName;

    private String level;

    private Boolean isFree;

    private BigDecimal price;

    private String status;

    private String rejectionReason;

    private Boolean isPublished;

    private LocalDateTime publishedAt;

    private Integer totalStudents;

    private BigDecimal averageRating;

    private UUID createdById;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
