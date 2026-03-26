package com.courselylabs.courselylab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewRequestDTO {

    @NotNull
    private Integer rating;

    @NotBlank
    private String comment;
}