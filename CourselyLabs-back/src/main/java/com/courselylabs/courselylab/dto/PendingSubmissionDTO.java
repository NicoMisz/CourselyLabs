package com.courselylabs.courselylab.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingSubmissionDTO {
    private UUID id;
    private UUID attemptId;
    private String type;
    private String fileName;
    private Long fileSize;
    private String answerText;
    private LocalDateTime createdAt;

    private UUID studentId;
    private String studentName;

    private UUID courseId;
    private String courseTitle;

    private UUID lessonId;
    private String lessonTitle;

    private UUID blockId;
    private UUID assessmentId;
    private String assessmentType;
}
