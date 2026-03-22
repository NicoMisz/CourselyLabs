package com.courselylabs.courselylab.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonDTO {

    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Type is required")
    private String type;

    private String contentUrl;
    private String contentText;
    private Integer duration;
    private Integer position;
    private Boolean isFree;
    private UUID sectionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
