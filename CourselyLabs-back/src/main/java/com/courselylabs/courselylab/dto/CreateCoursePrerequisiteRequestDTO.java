package com.courselylabs.courselylab.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCoursePrerequisiteRequestDTO {

    @NotNull
    private UUID prerequisiteCourseId;

    @Min(70)
    @Max(90)
    private int completionThreshold = 80; // valor por defecto: 80%
}
