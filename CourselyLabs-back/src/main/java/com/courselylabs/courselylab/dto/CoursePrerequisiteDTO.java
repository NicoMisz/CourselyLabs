package com.courselylabs.courselylab.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursePrerequisiteDTO {
    private UUID id;
    private UUID courseId;
    private UUID prerequisiteCourseId;
    private String prerequisiteTitle;
    private String prerequisiteSlug;
    private int completionThreshold; // entre 70 y 90
}