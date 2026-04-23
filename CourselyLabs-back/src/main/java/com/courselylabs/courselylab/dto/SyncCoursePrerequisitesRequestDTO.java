package com.courselylabs.courselylab.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SyncCoursePrerequisitesRequestDTO {
    private List<CreateCoursePrerequisiteRequestDTO> prerequisites = new ArrayList<>();
}
