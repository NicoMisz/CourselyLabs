package com.courselylabs.courselylab.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.PendingSubmissionDTO;
import com.courselylabs.courselylab.entity.AssessmentAttemptEntity;
import com.courselylabs.courselylab.entity.AssessmentEntity;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.LessonEntity;
import com.courselylabs.courselylab.entity.SubmissionEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.repository.SubmissionRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class GradingService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public GradingService(SubmissionRepository submissionRepository,
                          UserRepository userRepository) {
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    public List<PendingSubmissionDTO> findPendingForUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        List<SubmissionEntity> submissions = "admin".equals(user.getRole())
                ? submissionRepository.findAllPending()
                : submissionRepository.findPendingForGrader(user.getId());

        return submissions.stream().map(this::toDTO).toList();
    }

    private PendingSubmissionDTO toDTO(SubmissionEntity s) {
        PendingSubmissionDTO dto = new PendingSubmissionDTO();
        dto.setId(s.getId());
        dto.setType(s.getType());
        dto.setFileName(s.getFileName());
        dto.setFileSize(s.getFileSize());
        dto.setAnswerText(s.getAnswerText());
        dto.setCreatedAt(s.getCreatedAt());

        AssessmentAttemptEntity attempt = s.getAttempt();
        dto.setAttemptId(attempt.getId());

        UserEntity student = attempt.getUser();
        if (student != null) {
            dto.setStudentId(student.getId());
            dto.setStudentName((student.getFirstName() + " " + student.getLastName()).trim());
        }

        AssessmentEntity assessment = attempt.getAssessment();
        if (assessment != null) {
            dto.setAssessmentId(assessment.getId());
            dto.setAssessmentType(assessment.getType());
            if (assessment.getBlock() != null) dto.setBlockId(assessment.getBlock().getId());

            LessonEntity lesson = assessment.getLesson();
            if (lesson != null) {
                dto.setLessonId(lesson.getId());
                dto.setLessonTitle(lesson.getTitle());
                CourseEntity course = lesson.getSection().getCourse();
                dto.setCourseId(course.getId());
                dto.setCourseTitle(course.getTitle());
            }
        }
        return dto;
    }
}
