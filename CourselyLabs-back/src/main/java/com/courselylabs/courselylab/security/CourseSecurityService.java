package com.courselylabs.courselylab.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.entity.AssessmentAttemptEntity;
import com.courselylabs.courselylab.entity.AssessmentEntity;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.LessonEntity;
import com.courselylabs.courselylab.entity.LessonResourceEntity;
import com.courselylabs.courselylab.entity.SectionEntity;
import com.courselylabs.courselylab.entity.SubmissionEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.repository.AssessmentAttemptRepository;
import com.courselylabs.courselylab.repository.AssessmentRepository;
import com.courselylabs.courselylab.repository.CourseInstructorRepository;
import com.courselylabs.courselylab.repository.CourseRepository;
import com.courselylabs.courselylab.repository.EnrollmentRepository;
import com.courselylabs.courselylab.repository.LessonRepository;
import com.courselylabs.courselylab.repository.LessonResourceRepository;
import com.courselylabs.courselylab.repository.SectionRepository;
import com.courselylabs.courselylab.repository.SubmissionRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service("courseSecurityService")
@Transactional(readOnly = true)
public class CourseSecurityService {

    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final LessonResourceRepository resourceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final SubmissionRepository submissionRepository;

    public CourseSecurityService(CourseRepository courseRepository,
                                  CourseInstructorRepository courseInstructorRepository,
                                  UserRepository userRepository,
                                  SectionRepository sectionRepository,
                                  LessonRepository lessonRepository,
                                  LessonResourceRepository resourceRepository,
                                  EnrollmentRepository enrollmentRepository,
                                  AssessmentRepository assessmentRepository,
                                  AssessmentAttemptRepository attemptRepository,
                                  SubmissionRepository submissionRepository) {
        this.courseRepository = courseRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.lessonRepository = lessonRepository;
        this.resourceRepository = resourceRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
        this.submissionRepository = submissionRepository;
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

    public boolean canEditResource(UUID resourceId, Authentication auth) {
        LessonResourceEntity resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) return false;
        return isOwnerOrInstructorOrAdmin(resource.getLesson().getSection().getCourse().getId(), auth);
    }

    /**
     * Can the user access a lesson's content (including resources)?
     * Rules:
     * - Lesson is free: anyone authenticated
     * - Lesson is not free: user must be enrolled OR instructor/owner/admin
     * - Course is premium (not free) and user is not enrolled: also requires premium subscription
     */
    public boolean canAccessLesson(UUID lessonId, Authentication auth) {
        LessonEntity lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) return false;

        CourseEntity course = lesson.getSection().getCourse();
        UUID courseId = course.getId();

        UserEntity user = getUser(auth);

        // Free lesson — anyone authenticated
        if (Boolean.TRUE.equals(lesson.getIsFree())) {
            return user != null;
        }

        if (user == null) return false;

        // Instructor/owner/admin always access
        if (isAdmin(user) || isOwner(courseId, user) || isInstructor(courseId, user)) {
            return true;
        }

        // Student: must be enrolled
        return enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId);
    }

    public boolean canEditAssessment(UUID assessmentId, Authentication auth) {
        AssessmentEntity assessment = assessmentRepository.findById(assessmentId).orElse(null);
        if (assessment == null) return false;
        return isOwnerOrInstructorOrAdmin(assessment.getLesson().getSection().getCourse().getId(), auth);
    }

    public boolean canEditAssessmentByLesson(UUID lessonId, Authentication auth) {
        LessonEntity lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) return false;
        return isOwnerOrInstructorOrAdmin(lesson.getSection().getCourse().getId(), auth);
    }

    public boolean canAccessAssessment(UUID assessmentId, Authentication auth) {
        AssessmentEntity assessment = assessmentRepository.findById(assessmentId).orElse(null);
        if (assessment == null) return false;
        return canAccessLesson(assessment.getLesson().getId(), auth);
    }

    public boolean canAccessAttempt(UUID attemptId, Authentication auth) {
        AssessmentAttemptEntity attempt = attemptRepository.findById(attemptId).orElse(null);
        if (attempt == null) return false;
        UserEntity user = getUser(auth);
        if (user == null) return false;
        // Owner of attempt OR course instructor/admin
        if (attempt.getUser().getId().equals(user.getId())) return true;
        return isOwnerOrInstructorOrAdmin(
                attempt.getAssessment().getLesson().getSection().getCourse().getId(), auth);
    }

    public boolean canGradeSubmission(UUID submissionId, Authentication auth) {
        SubmissionEntity submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) return false;
        return isOwnerOrInstructorOrAdmin(
                submission.getAttempt().getAssessment().getLesson().getSection().getCourse().getId(), auth);
    }

    public boolean canAccessResource(UUID resourceId, Authentication auth) {
        LessonResourceEntity resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) return false;
        return canAccessLesson(resource.getLesson().getId(), auth);
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
