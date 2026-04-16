package com.courselylabs.courselylab.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.BlockedPrerequisiteDTO;
import com.courselylabs.courselylab.dto.CoursePrerequisiteDTO;
import com.courselylabs.courselylab.dto.CreateCoursePrerequisiteRequestDTO;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.CoursePrerequisiteEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.PrerequisiteConflictException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.exception.UnauthorizedException;
import com.courselylabs.courselylab.repository.CourseInstructorRepository;
import com.courselylabs.courselylab.repository.CoursePrerequisiteRepository;
import com.courselylabs.courselylab.repository.CourseRepository;
import com.courselylabs.courselylab.repository.EnrollmentRepository;
import com.courselylabs.courselylab.repository.LessonProgressRepository;
import com.courselylabs.courselylab.repository.SectionRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional
public class CoursePrerequisiteService {

    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;

    public CoursePrerequisiteService(
            CoursePrerequisiteRepository prerequisiteRepository,
            CourseInstructorRepository courseInstructorRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            LessonProgressRepository lessonProgressRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository) {
        this.prerequisiteRepository = prerequisiteRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
    }

    // --- Lectura (solo premium o admin) ---

    @Transactional(readOnly = true)
    public List<CoursePrerequisiteDTO> findByCourseId(UUID courseId, String email) {
            assertIsPremiumOrAdmin(email);
            return prerequisiteRepository.findWithPrerequisiteCourseByCourseId(courseId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // --- Gestion (solo instructor del curso o admin) ---

    public CoursePrerequisiteDTO addPrerequisite(UUID courseId, CreateCoursePrerequisiteRequestDTO request, String email) {
        assertCanManagePrerequisites(courseId, email);

        CourseEntity course = requireCourse(courseId);
        CourseEntity prerequisiteCourse = requireCourse(request.getPrerequisiteCourseId());

        if (course.getId().equals(prerequisiteCourse.getId())) {
            throw new BadRequestException("A course cannot be its own prerequisite");
        }

        if (prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId(courseId, prerequisiteCourse.getId())) {
            throw new BadRequestException("This prerequisite already exists");
        }

        if (wouldCreateCycle(courseId, prerequisiteCourse.getId())) {
            throw new BadRequestException("Adding this prerequisite would create a cycle");
        }

        CoursePrerequisiteEntity entity = new CoursePrerequisiteEntity();
        entity.setCourse(course);
        entity.setPrerequisiteCourse(prerequisiteCourse);
        entity.setCompletionThreshold(request.getCompletionThreshold());

        return toDTO(prerequisiteRepository.save(entity));
    }

    public void deletePrerequisite(UUID courseId, UUID prereqId, String email) {
        assertCanManagePrerequisites(courseId, email);

        CoursePrerequisiteEntity entity = prerequisiteRepository
                .findByCourseIdAndPrerequisiteCourseId(courseId, prereqId)
                .orElseThrow(() -> new ResourceNotFoundException("Prerequisite", "courseId/prerequisiteCourseId",
                        courseId + "/" + prereqId));
        prerequisiteRepository.delete(entity);
    }

    // --- Blockers (solo premium o admin) ---

    @Transactional(readOnly = true)
    public List<BlockedPrerequisiteDTO> findBlockedPrerequisites(UUID courseId, String email) {
            UserEntity user = requirePremiumOrAdminUser(email);

            return prerequisiteRepository.findWithPrerequisiteCourseByCourseId(courseId)
                .stream()
                .map(prereq -> toBlockedPrerequisiteDTO(
                        user.getId(),
                        prereq.getPrerequisiteCourse(),
                        prereq.getCompletionThreshold()))
                .filter(blocked -> !blocked.isCompleted())
                .toList();
    }

    // --- Validacion al inscribirse ---

    public void assertCanEnroll(String email, UUID courseId) {
        // Solo aplica la logica de prerequisitos a usuarios premium o admin.
        // Usuarios free no tienen acceso a esta feature: se les deniega directamente.
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        boolean isPremiumOrAdmin = "premium".equalsIgnoreCase(user.getRole())
                || "admin".equalsIgnoreCase(user.getRole());

        if (!isPremiumOrAdmin) {
            throw new UnauthorizedException("Prerequisites are a premium feature. Upgrade your plan to access this course.");
        }

        List<BlockedPrerequisiteDTO> blocked = findBlockedPrerequisites(courseId, email);
        if (!blocked.isEmpty()) {
            throw new PrerequisiteConflictException(
                    "You must reach the required progress threshold in all prerequisite courses before enrolling",
                    blocked);
        }
    }

    // --- Helpers privados ---

    private boolean isCourseCompletedByUser(UUID userId, UUID courseId, int threshold) {
        int totalLessons = sectionRepository.findByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .mapToInt(section -> section.getLessons().size())
                .sum();

        if (totalLessons == 0) {
            return false;
        }

        int completedLessons = lessonProgressRepository.countCompletedByUserIdAndCourseId(userId, courseId);
        int progressPercent = (completedLessons * 100) / totalLessons;
        return progressPercent >= threshold;
    }

    private CourseEntity requireCourse(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
    }

    private UserEntity requirePremiumOrAdminUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        assertIsPremiumOrAdmin(user);
        return user;
    }

    private void assertIsPremiumOrAdmin(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        assertIsPremiumOrAdmin(user);
    }

    private void assertIsPremiumOrAdmin(UserEntity user) {
        boolean allowed = "premium".equalsIgnoreCase(user.getRole())
                || "admin".equalsIgnoreCase(user.getRole());
        if (!allowed) {
            throw new UnauthorizedException("This feature is only available for premium users");
        }
    }

    private void assertCanManagePrerequisites(UUID courseId, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if ("admin".equalsIgnoreCase(user.getRole())) {
            return;
        }

        boolean isInstructor = courseInstructorRepository.existsByCourseIdAndInstructorId(courseId, user.getId());
        if (!isInstructor) {
            throw new UnauthorizedException("Only the course instructor or an admin can manage prerequisites");
        }
    }

    private boolean wouldCreateCycle(UUID courseId, UUID prerequisiteCourseId) {
        return hasPath(prerequisiteCourseId, courseId, new HashSet<>());
    }

    private boolean hasPath(UUID fromCourseId, UUID targetCourseId, Set<UUID> visited) {
        if (!visited.add(fromCourseId)) {
            return false;
        }

        if (fromCourseId.equals(targetCourseId)) {
            return true;
        }

        for (CoursePrerequisiteEntity prereq : prerequisiteRepository.findByCourseId(fromCourseId)) {
            if (hasPath(prereq.getPrerequisiteCourse().getId(), targetCourseId, visited)) {
                return true;
            }
        }

        return false;
    }

    private CoursePrerequisiteDTO toDTO(CoursePrerequisiteEntity entity) {
        CourseEntity prerequisite = entity.getPrerequisiteCourse();
        return new CoursePrerequisiteDTO(
                entity.getId(),
                entity.getCourse().getId(),
                prerequisite.getId(),
                prerequisite.getTitle(),
                prerequisite.getSlug(),
                entity.getCompletionThreshold());
    }

    private BlockedPrerequisiteDTO toBlockedPrerequisiteDTO(UUID userId, CourseEntity course, int threshold) {
        int totalLessons = sectionRepository.findByCourseIdOrderByPositionAsc(course.getId())
                .stream()
                .mapToInt(section -> section.getLessons().size())
                .sum();
        int completedLessons = lessonProgressRepository.countCompletedByUserIdAndCourseId(userId, course.getId());
        boolean enrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, course.getId());
        int progressPercent = totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0;
        boolean completed = progressPercent >= threshold;

        return new BlockedPrerequisiteDTO(
                course.getId(),
                course.getTitle(),
                course.getSlug(),
                enrolled,
                completed,
                completedLessons,
                totalLessons,
                progressPercent,
                threshold);
    }
}