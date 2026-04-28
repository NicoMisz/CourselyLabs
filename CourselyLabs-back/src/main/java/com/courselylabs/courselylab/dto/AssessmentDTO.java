package com.courselylabs.courselylab.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentDTO {
    private UUID id;
    private UUID lessonId;
    private UUID blockId;
    private String type;
    private String description;
    private Integer maxAttempts;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Boolean shuffleOptions;
    private List<QuizQuestionDTO> questions;
}
