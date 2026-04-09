package com.courselylabs.courselylab.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.CreateReviewRequestDTO;
import com.courselylabs.courselylab.dto.ReviewDTO;
import com.courselylabs.courselylab.dto.UpdateReviewRequestDTO;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.ReviewEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.exception.UnauthorizedException;
import com.courselylabs.courselylab.mapper.ReviewMapper;
import com.courselylabs.courselylab.repository.CourseRepository;
import com.courselylabs.courselylab.repository.EnrollmentRepository;
import com.courselylabs.courselylab.repository.LessonProgressRepository;
import com.courselylabs.courselylab.repository.ReviewRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ReviewMapper reviewMapper;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository,
                         CourseRepository courseRepository, EnrollmentRepository enrollmentRepository,
                         LessonProgressRepository lessonProgressRepository, ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.reviewMapper = reviewMapper;
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> findByCourseId(UUID courseId) {
        return reviewMapper.toDTOList(reviewRepository.findByCourseId(courseId));
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> findByCourseId(UUID courseId, Pageable pageable) {
        return reviewRepository.findByCourseId(courseId, pageable)
                .map(reviewMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> findByUserId(UUID userId) {
        return reviewMapper.toDTOList(reviewRepository.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public ReviewDTO findById(UUID id) {
        ReviewEntity entity = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        return reviewMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(UUID courseId) {
        return reviewRepository.getAverageRatingByCourseId(courseId);
    }

    public ReviewDTO create(ReviewDTO dto) {
        if (reviewRepository.existsByCourseIdAndUserId(dto.getCourseId(), dto.getUserId())) {
            throw new BadRequestException("User has already reviewed this course");
        }

        if (!enrollmentRepository.existsByUserIdAndCourseId(dto.getUserId(), dto.getCourseId())) {
            throw new BadRequestException("User must be enrolled in the course to leave a review");
        }

        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));

        CourseEntity course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", dto.getCourseId()));

        ReviewEntity entity = new ReviewEntity();
        entity.setUser(user);
        entity.setCourse(course);
        entity.setRating(dto.getRating());
        entity.setComment(dto.getComment());

        entity = reviewRepository.save(entity);
        return reviewMapper.toDTO(entity);
    }

    public ReviewDTO update(UUID id, ReviewDTO dto) {
        ReviewEntity entity = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        reviewMapper.updateEntityFromDTO(dto, entity);
        entity = reviewRepository.save(entity);
        return reviewMapper.toDTO(entity);
    }

    public void delete(UUID id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review", "id", id);
        }
        reviewRepository.deleteById(id);
    }

    public ReviewDTO createForCourse(UUID courseId, CreateReviewRequestDTO dto, String email) {
    UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (reviewRepository.existsByCourseIdAndUserId(courseId, user.getId())) {
            throw new BadRequestException("User has already reviewed this course");
        }

        if (!enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new BadRequestException("User must be enrolled in the course to leave a review");
        }

        int completed = lessonProgressRepository.countCompletedByUserIdAndCourseId(user.getId(), courseId);
        if (completed < 1) {
            throw new BadRequestException("Complete at least one lesson before reviewing");
        }

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        ReviewEntity entity = new ReviewEntity();
        entity.setUser(user);
        entity.setCourse(course);
        entity.setRating(dto.getRating());
        entity.setComment(dto.getComment());

        ReviewDTO result = reviewMapper.toDTO(reviewRepository.save(entity));
        updateCourseAverageRating(courseId); // ← recalcula después de guardar
        return result;
    }

    @Transactional(readOnly = true)
    public ReviewDTO getMyReview(UUID courseId, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        ReviewEntity entity = reviewRepository.findByCourseIdAndUserId(courseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review", "courseId/userId", courseId + "/" + user.getId()));

        return reviewMapper.toDTO(entity);
    }

    public ReviewDTO updateOwn(UUID id, UpdateReviewRequestDTO dto, String email) {
        ReviewEntity entity = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        if (!entity.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedException("You can only modify your own review");
        }

        entity.setRating(dto.getRating());
        entity.setComment(dto.getComment());

        ReviewDTO result = reviewMapper.toDTO(reviewRepository.save(entity));
        updateCourseAverageRating(entity.getCourse().getId()); // ← recalcula después de actualizar
        return result;
    }


    public void deleteOwn(UUID id, String email) {
        ReviewEntity entity = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        // Validación de propiedad
        if (!entity.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedException("You can only delete your own review");
        }

        UUID courseId = entity.getCourse().getId(); // ← guardar antes de borrar
        reviewRepository.delete(entity);
        updateCourseAverageRating(courseId); // ← añadir
    }
    
    // Método auxiliar privado
    private void updateCourseAverageRating(UUID courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        
        Double avg = reviewRepository.getAverageRatingByCourseId(courseId);
        course.setAverageRating(avg != null ? BigDecimal.valueOf(avg) : BigDecimal.ZERO);
        courseRepository.save(course);
    }
}
