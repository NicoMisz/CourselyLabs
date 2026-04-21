package com.courselylabs.courselylab.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RelatedCourseDTO {
    private UUID courseId;
    private String title;
    private String slug;
    private int completionThreshold;
}