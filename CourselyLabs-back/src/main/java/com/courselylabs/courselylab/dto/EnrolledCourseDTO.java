package com.courselylabs.courselylab.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrolledCourseDTO {
    
    private UUID enrollmentId;
    private UUID courseId;
    private String slug;
    private String title;
    private String shortDescription;
    private String thumbnailUrl;
    private String level;
    private Boolean isFree;
    private BigDecimal price;
    private Integer progressPercent;
    private String progressStatus;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastAccessedAt;
}
