package com.courselylabs.courselylab.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockedPrerequisiteDTO {
    private UUID courseId;
    private String title;
    private String slug;
    private boolean enrolled;
    private boolean completed;      // true si supera el threshold
    private int completedLessons;
    private int totalLessons;
    private int progressPercent;    // progreso actual del usuario (0-100)
    private int requiredThreshold;  // umbral requerido para este prerequisito (70-90)
}
