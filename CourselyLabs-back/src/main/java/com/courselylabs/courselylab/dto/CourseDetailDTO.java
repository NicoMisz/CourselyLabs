package com.courselylabs.courselylab.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetailDTO {

    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String shortDescription;
    private String thumbnailUrl;

    private Integer categoryId;
    private String categoryName;

    private String level;
    private Boolean isFree;
    private BigDecimal price;

    private String status;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer totalStudents;
    private BigDecimal averageRating;
    private Long storageBytes;
    private List<InstructorSummaryDTO> instructors;
    private List<SectionDTO> sections;

    private List<CoursePrerequisiteDTO> prerequisites;
}