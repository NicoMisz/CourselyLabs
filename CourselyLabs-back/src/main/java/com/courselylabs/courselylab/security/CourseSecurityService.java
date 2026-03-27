package com.courselylabs.courselylab.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.LessonEntity;
import com.courselylabs.courselylab.entity.SectionEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.repository.CourseInstructorRepository;
import com.courselylabs.courselylab.repository.CourseRepository;
import com.courselylabs.courselylab.repository.LessonRepository;
import com.courselylabs.courselylab.repository.SectionRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service("courseSecurityService")
@Transactional(readOnly = true)
public class CourseSecurityService {

    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;

    public CourseSecurityService(CourseRepository courseRepository,
                                  CourseInstructorRepository courseInstructorRepository,
                                  UserRepository userRepository,
                                  SectionRepository sectionRepository,
                                  LessonRepository lessonRepository) {
        this.courseRepository = courseRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.lessonRepository = lessonRepository;
    }

    public boolean isOwnerOrInstructor(UUID courseId, Authentication auth) {
        UserEntity user = getUser(auth);
        if (user == null) return false;

        return isOwner(courseId, user) || isInstructor(courseId, user);
    }

    public boolean isOwnerOrAdmin(UUID courseId, Authentication auth) {
        UserEntity user = getUser(auth);
        if (user == null) return false;

        return isAdmin(user) || isOwner(courseId, user);
    }

    public boolean isOwnerOrInstructorOrAdmin(UUID courseId, Authentication auth) {
        UserEntity user = getUser(auth);
        if (user == null) return false;

        return isAdmin(user) || isOwner(courseId, user) || isInstructor(courseId, user);
    }

    public boolean canEditSection(UUID sectionId, Authentication auth) {
        SectionEntity section = sectionRepository.findById(sectionId).orElse(null);
        if (section == null) return false;
        return isOwnerOrInstructorOrAdmin(section.getCourse().getId(), auth);
    }

    public boolean canEditLesson(UUID lessonId, Authentication auth) {
        LessonEntity lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) return false;
        return isOwnerOrInstructorOrAdmin(lesson.getSection().getCourse().getId(), auth);
    }

    private boolean isOwner(UUID courseId, UserEntity user) {
        return courseRepository.findById(courseId)
                .map(CourseEntity::getCreatedBy)
                .map(owner -> owner.getId().equals(user.getId()))
                .orElse(false);
    }

    private boolean isInstructor(UUID courseId, UserEntity user) {
        return courseInstructorRepository.existsByCourseIdAndInstructorId(courseId, user.getId());
    }

    private boolean isAdmin(UserEntity user) {
        return "admin".equals(user.getRole());
    }

    private UserEntity getUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
