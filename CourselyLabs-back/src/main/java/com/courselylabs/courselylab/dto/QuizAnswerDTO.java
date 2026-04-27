package com.courselylabs.courselylab.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizAnswerDTO {
    private UUID questionId;
    /** May be null if the user skipped. */
    private UUID selectedOptionId;
}
