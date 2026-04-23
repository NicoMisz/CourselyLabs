package com.courselylabs.courselylab.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseRelatedResponseDTO {
    private List<RelatedCourseDTO> prerequisites;
    private List<RelatedCourseDTO> requiredBy;
}