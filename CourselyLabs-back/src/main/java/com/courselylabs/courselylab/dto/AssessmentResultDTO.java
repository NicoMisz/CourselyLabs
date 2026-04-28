package com.courselylabs.courselylab.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentResultDTO {
    private UUID attemptId;
    private Integer score;
    private Integer maxScore;
    private Boolean passed;
    private Integer passingScore;
    /** Per-question feedback with full options (incl. isCorrect + explanation). */
    private List<QuizQuestionDTO> questions;
    /** Per-question selected option (matches questions order). */
    private List<QuizAnswerDTO> answers;
}
