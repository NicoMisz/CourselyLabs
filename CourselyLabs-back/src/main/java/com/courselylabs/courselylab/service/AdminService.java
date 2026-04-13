package com.courselylabs.courselylab.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import com.courselylabs.courselylab.dto.CourseDTO;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.mapper.CourseMapper;
import com.courselylabs.courselylab.repository.CourseRepository;
import com.courselylabs.courselylab.repository.EnrollmentRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final EnrollmentRepository enrollmentRepository;

    public AdminService(UserRepository userRepository,
                        CourseRepository courseRepository,
                        CourseMapper courseMapper,
                        EnrollmentRepository enrollmentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.enrollmentRepository = enrollmentRepository;
    }

    // --- Stats ---

    @Transactional(readOnly = true)
    public AdminStats getStats() {
        long totalUsers = userRepository.count();
        long totalPublishedCourses = courseRepository.findByIsPublishedTrue().size();
        long pendingCourses = courseRepository.findByStatus("pending_review").size();
        long totalEnrollments = enrollmentRepository.count();

        return new AdminStats(totalUsers, totalPublishedCourses, pendingCourses, totalEnrollments);
    }

    public record AdminStats(long totalUsers, long publishedCourses, long pendingCourses, long totalEnrollments) {}

    // --- Users ---

    @Transactional(readOnly = true)
    public Page<UserEntity> getUsers(String search, String role, Boolean isActive, Pageable pageable) {
        Specification<UserEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
                ));
            }
            if (role != null && !role.isBlank()) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable);
    }

    public UserEntity changeRole(UUID userId, String newRole) {
        if (!List.of("user", "premium", "admin").contains(newRole)) {
            throw new BadRequestException("Rol no valido: " + newRole);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setRole(newRole);
        return userRepository.save(user);
    }

    public UserEntity banUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if ("admin".equals(user.getRole())) {
            throw new BadRequestException("No se puede banear a un administrador");
        }

        user.setIsActive(false);
        return userRepository.save(user);
    }

    public UserEntity unbanUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsActive(true);
        return userRepository.save(user);
    }

    // --- Course moderation ---

    @Transactional(readOnly = true)
    public List<CourseDTO> getPendingCourses() {
        return courseMapper.toDTOList(courseRepository.findByStatus("pending_review"));
    }

    public CourseDTO approveCourse(UUID courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (!"pending_review".equals(course.getStatus())) {
            throw new BadRequestException("El curso no esta pendiente de revision");
        }

        course.setStatus("published");
        course.setIsPublished(true);
        course.setPublishedAt(LocalDateTime.now());
        return courseMapper.toDTO(courseRepository.save(course));
    }

    public CourseDTO rejectCourse(UUID courseId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("El motivo del rechazo es obligatorio");
        }

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (!"pending_review".equals(course.getStatus())) {
            throw new BadRequestException("El curso no esta pendiente de revision");
        }

        // Accumulate rejection reasons
        String existing = course.getRejectionReason();
        if (existing != null && !existing.isBlank()) {
            // Count existing reasons to number the new one
            long count = existing.chars().filter(ch -> ch == ':').count();
            course.setRejectionReason(existing + "; " + (count + 1) + ": " + reason);
        } else {
            course.setRejectionReason("1: " + reason);
        }

        course.setStatus("rejected");
        course.setIsPublished(false);
        return courseMapper.toDTO(courseRepository.save(course));
    }
}
