package com.courselylabs.courselylab.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.EnrollRequestDTO;
import com.courselylabs.courselylab.dto.EnrolledCourseDTO;
import com.courselylabs.courselylab.dto.EnrollmentDTO;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.EnrollmentEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.mapper.EnrollmentMapper;
import com.courselylabs.courselylab.repository.CourseRepository;
import com.courselylabs.courselylab.repository.EnrollmentRepository;
import com.courselylabs.courselylab.repository.LessonProgressRepository;
import com.courselylabs.courselylab.repository.SectionRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final LessonProgressRepository lessonProgressRepository;
    private final SectionRepository sectionRepository;
    private final CoursePrerequisiteService coursePrerequisiteService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, UserRepository userRepository,
            CourseRepository courseRepository, EnrollmentMapper enrollmentMapper,
            LessonProgressRepository lessonProgressRepository,
            SectionRepository sectionRepository,
            CoursePrerequisiteService coursePrerequisiteService) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentMapper = enrollmentMapper;
        this.lessonProgressRepository = lessonProgressRepository;
        this.sectionRepository = sectionRepository;
        this.coursePrerequisiteService = coursePrerequisiteService;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDTO> findByUserId(UUID userId) {
        return enrollmentMapper.toDTOList(enrollmentRepository.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentDTO> findByUserId(UUID userId, Pageable pageable) {
        return enrollmentRepository.findByUserId(userId, pageable)
                .map(enrollmentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentDTO> findByCourseId(UUID courseId, Pageable pageable) {
        return enrollmentRepository.findByCourseId(courseId, pageable)
                .map(enrollmentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public EnrollmentDTO findById(UUID id) {
        EnrollmentEntity entity = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
        return enrollmentMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public boolean isEnrolled(UUID userId, UUID courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    public EnrollmentDTO enroll(EnrollmentDTO dto) {
        if (isEnrolled(dto.getUserId(), dto.getCourseId())) {
            throw new BadRequestException("User is already enrolled in this course");
        }

        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));

        CourseEntity course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", dto.getCourseId()));

        if (!course.getIsPublished()) {
            throw new BadRequestException("Cannot enroll in an unpublished course");
        }

        EnrollmentEntity entity = new EnrollmentEntity();
        entity.setUser(user);
        entity.setCourse(course);
        entity.setAccessType(course.getIsFree() ? "free" : "paid");

        entity = enrollmentRepository.save(entity);
        return enrollmentMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserEnrolled(String email, UUID courseId) {
        UserEntity user = getUserByEmail(email);
        return enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId);
    }

    public EnrollmentDTO enrollCurrentUser(String email, EnrollRequestDTO request) {
        UserEntity user = getUserByEmail(email);

        CourseEntity course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        if (!Boolean.TRUE.equals(course.getIsPublished())) {
            throw new BadRequestException("No puedes inscribirte en un curso no publicado");
        }

        if (!Boolean.TRUE.equals(course.getIsFree())) {
            throw new BadRequestException("Este curso requiere pago previo antes de la inscripcion");
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            throw new BadRequestException("Ya estas inscrito en este curso");
        }

        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setAccessType("free");

        EnrollmentEntity saved = enrollmentRepository.save(enrollment);
        course.setTotalStudents((course.getTotalStudents() == null ? 0 : course.getTotalStudents()) + 1);

        return enrollmentMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrolledCourseDTO> findMyCourses(String email) {
        UserEntity user = getUserByEmail(email);

        return enrollmentRepository.findAllByUserIdOrderByLastAccessedAtDesc(user.getId())
                .stream()
                .map(this::toEnrolledCourseDTO)
                .sorted(Comparator.comparing(
                        EnrolledCourseDTO::getLastAccessedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private EnrolledCourseDTO toEnrolledCourseDTO(EnrollmentEntity entity) {
        CourseEntity course = entity.getCourse();
        UUID userId = entity.getUser().getId();
        UUID courseId = course.getId();

        int totalLessons = sectionRepository.findByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .mapToInt(s -> s.getLessons().size())
                .sum();

        int completed = lessonProgressRepository.countCompletedByUserIdAndCourseId(userId, courseId);
        int progressPercent = totalLessons > 0 ? (completed * 100) / totalLessons : 0;

        String progressStatus;
        if (progressPercent >= 100) {
            progressStatus = "completado";
        } else if (progressPercent > 0) {
            progressStatus = "en-curso";
        } else {
            progressStatus = "nuevo";
        }

        return new EnrolledCourseDTO(
                entity.getId(),
                courseId,
                course.getSlug(),
                course.getTitle(),
                course.getShortDescription(),
                course.getThumbnailUrl(),
                course.getLevel(),
                course.getIsFree(),
                course.getPrice(),
                progressPercent,
                progressStatus,
                entity.getEnrolledAt(),
                entity.getLastAccessedAt());
    }

    public EnrollmentDTO updateLastAccessed(UUID id) {
        EnrollmentEntity entity = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
        entity.setLastAccessedAt(LocalDateTime.now());
        entity = enrollmentRepository.save(entity);
        return enrollmentMapper.toDTO(entity);
    }

    public void unenroll(UUID userId, UUID courseId) {
        EnrollmentEntity entity = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        enrollmentRepository.delete(entity);
    }

    public void delete(UUID id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Enrollment", "id", id);
        }
        enrollmentRepository.deleteById(id);
    }

    public void updateLastAccessForCurrentUser(String email, UUID courseId) {
        UserEntity user = getUserByEmail(email);

        EnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "courseId", courseId));

        enrollment.setLastAccessedAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);
    }
}
