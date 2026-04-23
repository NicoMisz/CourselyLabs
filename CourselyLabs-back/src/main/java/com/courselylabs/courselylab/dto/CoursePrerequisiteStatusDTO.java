package com.courselylabs.courselylab.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoursePrerequisiteStatusDTO {
        private UUID prerequisiteCourseId;
        private String title;
        private String slug;
        private boolean enrolled;
        private boolean completed;
        private int completedLessons;
        private int totalLessons;
        private int progressPercent;
        private int requiredThreshold;
}