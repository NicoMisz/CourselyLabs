package com.courselylabs.courselylab.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public CourseService(
            CourseRepository courseRepository,
            CategoriaRepository categoriaRepository,
            CourseMapper courseMapper,
            SectionMapper sectionMapper,
            ReviewRepository reviewRepository,
            EnrollmentRepository enrollmentRepository,
            CourseInstructorRepository courseInstructorRepository,
            SectionRepository sectionRepository) {
        this.courseRepository = courseRepository;
        this.categoriaRepository = categoriaRepository;
        this.courseMapper = courseMapper;
        this.sectionMapper = sectionMapper;
        this.reviewRepository = reviewRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseInstructorRepository = courseInstructorRepository;
        this.sectionRepository = sectionRepository;
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
    public CourseDetailDTO findById(UUID id) {
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return buildCourseDetail(entity);
    }

    @Transactional(readOnly = true)
    public CourseDetailDTO findBySlug(String slug) {
        CourseEntity entity = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "slug", slug));
        return buildCourseDetail(entity);
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

    public CourseDTO create(CourseDTO dto) {
        if (courseRepository.existsBySlug(dto.getSlug())) {
            throw new BadRequestException("Course with slug '" + dto.getSlug() + "' already exists");
        }

        CourseEntity entity = courseMapper.toEntity(dto);

        if (dto.getCategoryId() != null) {
            CategoriaEntity category = categoriaRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            entity.setCategory(category);
        }

        entity.setStatus("draft");
        entity.setIsPublished(false);
        entity = courseRepository.save(entity);
        return courseMapper.toDTO(entity);
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

    private CourseDetailDTO buildCourseDetail(CourseEntity entity) {
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
}
