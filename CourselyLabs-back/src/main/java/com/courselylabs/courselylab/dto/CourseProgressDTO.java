package com.courselylabs.courselylab.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseProgressDTO {

    private UUID courseId;
    private int totalLessons;
    private int completedLessons;
    private int progressPercent;
    private List<UUID> completedLessonIds;
}
