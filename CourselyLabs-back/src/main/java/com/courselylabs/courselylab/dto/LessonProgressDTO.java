package com.courselylabs.courselylab.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonProgressDTO {

    private UUID id;
    private UUID lessonId;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private Integer lastPositionSeconds;
}
