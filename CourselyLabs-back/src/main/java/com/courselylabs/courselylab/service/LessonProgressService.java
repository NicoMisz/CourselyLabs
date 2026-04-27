package com.courselylabs.courselylab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.CourseProgressDTO;
import com.courselylabs.courselylab.dto.LessonProgressDTO;
import com.courselylabs.courselylab.entity.LessonEntity;
import com.courselylabs.courselylab.entity.LessonProgressEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.repository.LessonProgressRepository;
import com.courselylabs.courselylab.repository.LessonRepository;
import com.courselylabs.courselylab.repository.SectionRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional
public class LessonProgressService {

    private final LessonProgressRepository progressRepository;
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;

    public LessonProgressService(LessonProgressRepository progressRepository,
                                  LessonRepository lessonRepository,
                                  SectionRepository sectionRepository,
                                  UserRepository userRepository) {
        this.progressRepository = progressRepository;
        this.lessonRepository = lessonRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Force-marks a lesson as completed for a given user. Used by the assessment system
     * after passing a quiz / instructor grading a project or open-text submission.
     */
    public void markCompleted(UUID userId, UUID lessonId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        LessonProgressEntity progress = progressRepository
                .findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    LessonProgressEntity p = new LessonProgressEntity();
                    p.setUser(user);
                    p.setLesson(lesson);
                    return p;
                });

        if (!Boolean.TRUE.equals(progress.getIsCompleted())) {
            progress.setIsCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            progressRepository.save(progress);
        }
    }

    public LessonProgressDTO toggleComplete(String email, UUID lessonId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        LessonProgressEntity progress = progressRepository
                .findByUserIdAndLessonId(user.getId(), lessonId)
                .orElseGet(() -> {
                    LessonProgressEntity p = new LessonProgressEntity();
                    p.setUser(user);
                    p.setLesson(lesson);
                    return p;
                });

        boolean newState = !Boolean.TRUE.equals(progress.getIsCompleted());
        progress.setIsCompleted(newState);
        progress.setCompletedAt(newState ? LocalDateTime.now() : null);

        progress = progressRepository.save(progress);
        return toDTO(progress);
    }

    public LessonProgressDTO updatePosition(String email, UUID lessonId, int positionSeconds) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        LessonProgressEntity progress = progressRepository
                .findByUserIdAndLessonId(user.getId(), lessonId)
                .orElseGet(() -> {
                    LessonProgressEntity p = new LessonProgressEntity();
                    p.setUser(user);
                    p.setLesson(lesson);
                    return p;
                });

        progress.setLastPositionSeconds(positionSeconds);
        progress = progressRepository.save(progress);
        return toDTO(progress);
    }

    @Transactional(readOnly = true)
    public CourseProgressDTO getCourseProgress(String email, UUID courseId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        int totalLessons = sectionRepository.findByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .mapToInt(s -> s.getLessons().size())
                .sum();

        int completed = progressRepository.countCompletedByUserIdAndCourseId(user.getId(), courseId);

        List<UUID> completedIds = progressRepository
                .findByUserIdAndCourseId(user.getId(), courseId)
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCompleted()))
                .map(p -> p.getLesson().getId())
                .collect(Collectors.toList());

        int percent = totalLessons > 0 ? (completed * 100) / totalLessons : 0;

        return new CourseProgressDTO(courseId, totalLessons, completed, percent, completedIds);
    }

    @Transactional(readOnly = true)
    public LessonProgressDTO getLessonProgress(String email, UUID lessonId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return progressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .map(this::toDTO)
                .orElse(new LessonProgressDTO(null, lessonId, false, null, 0));
    }

    private LessonProgressDTO toDTO(LessonProgressEntity entity) {
        return new LessonProgressDTO(
                entity.getId(),
                entity.getLesson().getId(),
                entity.getIsCompleted(),
                entity.getCompletedAt(),
                entity.getLastPositionSeconds());
    }
}
