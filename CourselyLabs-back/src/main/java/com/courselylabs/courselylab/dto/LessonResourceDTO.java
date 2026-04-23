package com.courselylabs.courselylab.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonResourceDTO {
    private UUID id;
    private UUID lessonId;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Integer downloadCount;
    private Integer position;
    private LocalDateTime createdAt;
}
