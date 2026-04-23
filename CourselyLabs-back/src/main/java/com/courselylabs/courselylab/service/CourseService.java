package com.courselylabs.courselylab.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.CourseDTO;
import com.courselylabs.courselylab.dto.CourseDetailDTO;
import com.courselylabs.courselylab.dto.InstructorSummaryDTO;
import com.courselylabs.courselylab.entity.CategoriaEntity;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.CourseInstructorEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.mapper.CourseMapper;
import com.courselylabs.courselylab.mapper.SectionMapper;
import com.courselylabs.courselylab.repository.CategoriaRepository;
import com.courselylabs.courselylab.repository.CourseInstructorRepository;
import com.courselylabs.courselylab.repository.CourseRepository;
import com.courselylabs.courselylab.repository.EnrollmentRepository;
import com.courselylabs.courselylab.repository.ReviewRepository;
import com.courselylabs.courselylab.repository.SectionRepository;
import com.courselylabs.courselylab.repository.UserRepository;
import com.courselylabs.courselylab.repository.spec.CourseSpecifications;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoriaRepository categoriaRepository;
    private final CourseMapper courseMapper;
    private final SectionMapper sectionMapper;

    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final CoursePrerequisiteService coursePrerequisiteService;

    private static final int MAX_COURSES_USER = 2;
    private static final int MAX_COURSES_PREMIUM = 10;

    public CourseService(
            CourseRepository courseRepository,
            CategoriaRepository categoriaRepository,
            CourseMapper courseMapper,
            SectionMapper sectionMapper,
            ReviewRepository reviewRepository,
            EnrollmentRepository enrollmentRepository,
            CourseInstructorRepository courseInstructorRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            CoursePrerequisiteService coursePrerequisiteService) {
        this.courseRepository = courseRepository;
        this.categoriaRepository = categoriaRepository;
        this.courseMapper = courseMapper;
        this.sectionMapper = sectionMapper;
        this.reviewRepository = reviewRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.coursePrerequisiteService = coursePrerequisiteService;
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> findAll() {
        return courseMapper.toDTOList(courseRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> findAllPublished(Pageable pageable) {
        return courseRepository.findByIsPublishedTrue(pageable)
                .map(courseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public CourseDetailDTO findById(UUID id, String email) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        UserEntity currentUser = resolveUserOrNull(email);
        return buildCourseDetail(entity, currentUser);
    }

    @Transactional(readOnly = true)
    public CourseDetailDTO findBySlug(String slug, String email) {
        CourseEntity entity = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "slug", slug));
        UserEntity currentUser = resolveUserOrNull(email);
        return buildCourseDetail(entity, currentUser);
    }

    private UserEntity resolveUserOrNull(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<InstructorSummaryDTO> findInstructorsByCourseId(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        return mapInstructors(courseInstructorRepository.findByCourseId(courseId));
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> findByCategory(Integer categoryId, Pageable pageable) {
        return courseRepository.findByCategoryIdAndIsPublishedTrue(categoryId, pageable)
                .map(courseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> search(String keyword, Pageable pageable) {
        return courseRepository.searchPublishedCourses(keyword, pageable)
                .map(courseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> findByStatus(String status) {
        return courseMapper.toDTOList(courseRepository.findByStatus(status));
    }

    public CourseDTO create(CourseDTO dto, String email) {
        UserEntity creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Check course limit per role
        long currentCount = courseRepository.countByCreatedById(creator.getId());
        String role = creator.getRole() == null ? "user" : creator.getRole();
        if (!"admin".equals(role)) {
            int maxCourses = "premium".equals(role) ? MAX_COURSES_PREMIUM : MAX_COURSES_USER;
            if (currentCount >= maxCourses) {
                throw new BadRequestException(
                    "Has alcanzado el limite de cursos para tu plan (" + maxCourses + " cursos). Actualiza a premium para crear mas.");
            }
        }

        if (courseRepository.existsBySlug(dto.getSlug())) {
            throw new BadRequestException("Course with slug '" + dto.getSlug() + "' already exists");
        }

        CourseEntity entity = courseMapper.toEntity(dto);

        if (dto.getCategoryId() != null) {
            CategoriaEntity category = categoriaRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            entity.setCategory(category);
        }

        entity.setCreatedBy(creator);
        entity.setStatus("draft");
        entity.setIsPublished(false);
        entity = courseRepository.save(entity);

        // Auto-add creator as main instructor
        CourseInstructorEntity instructorLink = new CourseInstructorEntity();
        instructorLink.setCourse(entity);
        instructorLink.setInstructor(creator);
        instructorLink.setIsMain(true);
        courseInstructorRepository.save(instructorLink);

        return courseMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> findMyCourses(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return courseMapper.toDTOList(
                courseRepository.findByCreatedByIdOrderByCreatedAtDesc(user.getId()));
    }

    @Transactional(readOnly = true)
    public long countMyCourses(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return courseRepository.countByCreatedById(user.getId());
    }

    @Transactional(readOnly = true)
    public int getMaxCoursesForUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        String role = user.getRole() == null ? "user" : user.getRole();
        if ("admin".equals(role)) return Integer.MAX_VALUE;
        return "premium".equals(role) ? MAX_COURSES_PREMIUM : MAX_COURSES_USER;
    }

    public CourseDTO update(UUID id, CourseDTO dto) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        if (dto.getSlug() != null && !dto.getSlug().equals(entity.getSlug())) {
            if (courseRepository.existsBySlug(dto.getSlug())) {
                throw new BadRequestException("Course with slug '" + dto.getSlug() + "' already exists");
            }
        }

        if (dto.getCategoryId() != null) {
            CategoriaEntity category = categoriaRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            entity.setCategory(category);
        }

        courseMapper.updateEntityFromDTO(dto, entity);
        entity = courseRepository.save(entity);
        return courseMapper.toDTO(entity);
    }

    public CourseDTO publish(UUID id) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        entity.setIsPublished(true);
        entity.setStatus("published");
        entity.setPublishedAt(LocalDateTime.now());
        entity = courseRepository.save(entity);
        return courseMapper.toDTO(entity);
    }

    public CourseDTO unpublish(UUID id) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        entity.setIsPublished(false);
        entity.setStatus("draft");
        entity = courseRepository.save(entity);
        return courseMapper.toDTO(entity);
    }

    public CourseDTO submitForReview(UUID id) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        entity.setStatus("pending_review");
        entity = courseRepository.save(entity);
        return courseMapper.toDTO(entity);
    }

    public void delete(UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", "id", id);
        }
        courseRepository.deleteById(id);
    }

    private CourseDetailDTO buildCourseDetail(CourseEntity entity, UserEntity currentUser) {  // añadido currentUser
        CourseDetailDTO dto = new CourseDetailDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSlug(entity.getSlug());
        dto.setDescription(entity.getDescription());
        dto.setShortDescription(entity.getShortDescription());
        dto.setThumbnailUrl(entity.getThumbnailUrl());
        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
            dto.setCategoryName(entity.getCategory().getName());
        }
        dto.setLevel(entity.getLevel());
        dto.setIsFree(entity.getIsFree());
        dto.setPrice(entity.getPrice());
        dto.setStatus(entity.getStatus());
        dto.setIsPublished(entity.getIsPublished());
        dto.setPublishedAt(entity.getPublishedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        long students = enrollmentRepository.countByCourseId(entity.getId());
        dto.setTotalStudents((int) students);

        Double avg = reviewRepository.getAverageRatingByCourseId(entity.getId());
        BigDecimal avgRating = avg == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
        dto.setAverageRating(avgRating);

        List<CourseInstructorEntity> links = courseInstructorRepository.findByCourseId(entity.getId());
        dto.setInstructors(mapInstructors(links));
        dto.setSections(sectionMapper.toDTOList(
                sectionRepository.findByCourseIdOrderByPositionAsc(entity.getId())));

        // Prerequisitos solo para premium/admin
        boolean isPremiumOrAdmin = currentUser != null && (
            "premium".equalsIgnoreCase(currentUser.getRole())
            || "admin".equalsIgnoreCase(currentUser.getRole())
        );

        if (Boolean.TRUE.equals(entity.getIsFree())) {
            dto.setPrerequisites(List.of());
        } else if (isPremiumOrAdmin) {
            dto.setPrerequisites(coursePrerequisiteService.findByCourseId(entity.getId(), currentUser.getEmail()));
        } else {
            dto.setPrerequisites(List.of());
        }

        return dto;
    }

    private List<InstructorSummaryDTO> mapInstructors(List<CourseInstructorEntity> links) {
        List<InstructorSummaryDTO> instructors = new ArrayList<>();

        for (CourseInstructorEntity link : links) {
            UserEntity user = link.getInstructor();
            String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
            String last = user.getLastName() == null ? "" : user.getLastName().trim();
            String fullName = (first + " " + last).trim();

            instructors.add(new InstructorSummaryDTO(
                    user.getId(),
                    fullName,
                    user.getBio(),
                    user.getProfilePictureUrl()));
        }

        return instructors;
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> searchAdvanced(
            String keyword,
            Integer categoryId,
            String level,
            Boolean isFree,
            Double minRating,
            String sortBy,
            Pageable pageable
    ) {
        if (minRating != null && (minRating < 0 || minRating > 5)) {
            throw new BadRequestException("minRating debe estar entre 0 y 5");
        }

        CourseSearchSort sort = parseSortOrThrow(sortBy);
        String normalizedKeyword = normalizeLower(keyword);
        String normalizedLevel = normalizeLower(level);

        Sort resolvedSort = switch (sort) {
            case PRICE_ASC  -> Sort.by(Sort.Direction.ASC,  "price");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
            case POPULAR    -> Sort.by(Sort.Direction.DESC, "totalStudents"); // campo en entidad
            case RATING     -> Sort.by(Sort.Direction.DESC, "averageRating"); // campo en entidad
            default         -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable effectivePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                resolvedSort
        );

        var spec = CourseSpecifications.publishedWithFilters(
                normalizedKeyword,
                categoryId,
                normalizedLevel,
                isFree,
                minRating
        );

        return courseRepository.findAll(spec, effectivePageable)
                .map(courseMapper::toDTO);
    }

    private CourseSearchSort parseSortOrThrow(String sortBy) {
    try {
        return CourseSearchSort.fromQueryParam(sortBy);
    } catch (IllegalArgumentException ex) {
        throw new BadRequestException(ex.getMessage());
    }
    }

    private String normalize(String value) {
    if (value == null) {
        return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeLower(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
