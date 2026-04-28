package com.courselylabs.courselylab.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizQuestionDTO {
    private UUID id;
    private String questionText;
    private Integer position;
    private Integer points;
    private List<QuizOptionDTO> options;
}
