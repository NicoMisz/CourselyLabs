package com.courselylabs.courselylab.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionDTO {

    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private Integer position;
    private UUID courseId;
    private List<LessonDTO> lessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
