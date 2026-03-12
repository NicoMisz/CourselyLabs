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
    private String fullName;
    private String bio;
    private String profilePictureUrl;
}