package com.courselylabs.courselylab.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructorSummaryDTO {

    private UUID id;
    private String name;
    private String bio;
    private String avatarUrl;
    /** True si es el instructor principal del curso (course_instructors.is_main). */
    private Boolean isMain;
}
