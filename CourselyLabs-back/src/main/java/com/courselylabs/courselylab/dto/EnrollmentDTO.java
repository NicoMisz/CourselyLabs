package com.courselylabs.courselylab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentDTO {

    private UUID id;

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String userFullName;

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    private String courseTitle;

    @NotBlank(message = "Access type is required")
    private String accessType;

    private LocalDateTime enrolledAt;

    private LocalDateTime lastAccessedAt;
}
