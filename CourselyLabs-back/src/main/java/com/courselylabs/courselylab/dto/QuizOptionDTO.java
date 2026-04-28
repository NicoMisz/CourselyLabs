package com.courselylabs.courselylab.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizOptionDTO {
    private UUID id;
    private String optionText;
    /** Only populated in /results — never in public detail. */
    private Boolean isCorrect;
    /** Only populated in /results. */
    private String explanation;
    private Integer position;
}
