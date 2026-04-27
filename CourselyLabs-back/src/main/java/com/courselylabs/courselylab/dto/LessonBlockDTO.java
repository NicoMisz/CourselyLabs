package com.courselylabs.courselylab.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonBlockDTO {
    private UUID id;
    private UUID lessonId;
    private String type;
    private Integer position;
    private String textContent;
    private String videoUrl;
    private String pdfUrl;
    /** Populated when the block has an associated assessment (quiz/project/open_text). */
    private UUID assessmentId;
}
