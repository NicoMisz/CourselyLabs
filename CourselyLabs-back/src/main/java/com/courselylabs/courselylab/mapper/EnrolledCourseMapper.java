package com.courselylabs.courselylab.mapper;

import org.springframework.stereotype.Component;

import com.courselylabs.courselylab.dto.EnrolledCourseDTO;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.EnrollmentEntity;

@Component
public class EnrolledCourseMapper {

    public EnrolledCourseDTO toDTO(EnrollmentEntity enrollment) {
        CourseEntity course = enrollment.getCourse();

        return new EnrolledCourseDTO(
            enrollment.getId(),
            course.getId(),
            course.getSlug(),
            course.getTitle(),
            course.getShortDescription(),
            course.getThumbnailUrl(),
            course.getLevel(),
            course.getIsFree(),
            course.getPrice(),
            0,
            "nuevo",
            enrollment.getEnrolledAt(),
            enrollment.getLastAccessedAt()
        );
    }
}