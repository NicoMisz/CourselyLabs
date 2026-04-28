package com.courselylabs.courselylab.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentAttemptDTO {
    private UUID id;
    private UUID assessmentId;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Integer score;
    private Boolean passed;
    private Integer attemptNumber;
    private String status;
}
