package com.courselylabs.courselylab.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionDTO {
    private UUID id;
    private UUID attemptId;
    private String type;
    private String fileName;
    private Long fileSize;
    private String answerText;
    private String instructorFeedback;
    private LocalDateTime gradedAt;
    private UUID gradedById;
    private LocalDateTime createdAt;
    private String studentName;
    private UUID studentId;
}
