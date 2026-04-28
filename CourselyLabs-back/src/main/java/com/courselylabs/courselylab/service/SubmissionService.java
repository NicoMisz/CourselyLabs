package com.courselylabs.courselylab.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.courselylabs.courselylab.dto.SubmissionDTO;
import com.courselylabs.courselylab.entity.AssessmentAttemptEntity;
import com.courselylabs.courselylab.entity.AssessmentEntity;
import com.courselylabs.courselylab.entity.SubmissionEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.repository.AssessmentAttemptRepository;
import com.courselylabs.courselylab.repository.SubmissionRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional
public class SubmissionService {

    private static final long MAX_SUBMISSION_BYTES = 50L * 1024 * 1024; // 50 MB

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "text/plain", "text/markdown", "text/csv", "application/json",
            "application/zip", "application/x-zip-compressed"
    );

    private final SubmissionRepository submissionRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final FileStorageService storage;
    private final LessonProgressService progressService;

    public SubmissionService(SubmissionRepository submissionRepository,
                             AssessmentAttemptRepository attemptRepository,
                             UserRepository userRepository,
                             FileStorageService storage,
                             LessonProgressService progressService) {
        this.submissionRepository = submissionRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.storage = storage;
        this.progressService = progressService;
    }

    public SubmissionDTO submitProject(String email, UUID attemptId, MultipartFile file) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        AssessmentAttemptEntity attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", "id", attemptId));

        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Este intento no te pertenece");
        }
        if (!"in_progress".equals(attempt.getStatus())) {
            throw new BadRequestException("Este intento ya fue enviado");
        }

        AssessmentEntity assessment = attempt.getAssessment();
        if (!"project".equals(assessment.getType())) {
            throw new BadRequestException("Esta evaluación no es de tipo proyecto");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Archivo vacio");
        }
        if (file.getSize() > MAX_SUBMISSION_BYTES) {
            throw new BadRequestException("El archivo supera 50 MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Tipo de archivo no permitido: " + file.getContentType());
        }

        UUID courseId = assessment.getLesson().getSection().getCourse().getId();
        UUID lessonId = assessment.getLesson().getId();
        String storageKey = "submissions/" + courseId + "/" + lessonId + "/" + user.getId() + "/"
                + attemptId + "-" + sanitize(file.getOriginalFilename());

        try {
            storage.upload(storageKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new BadRequestException("Error leyendo el archivo");
        }

        SubmissionEntity entity = submissionRepository.findByAttemptId(attemptId).orElseGet(SubmissionEntity::new);
        // Replace previous file if exists
        if (entity.getStorageKey() != null && !entity.getStorageKey().equals(storageKey)) {
            storage.delete(entity.getStorageKey());
        }
        entity.setAttempt(attempt);
        entity.setType("project");
        entity.setStorageKey(storageKey);
        entity.setFileName(file.getOriginalFilename());
        entity.setFileSize(file.getSize());
        entity = submissionRepository.save(entity);

        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setStatus("submitted");
        attemptRepository.save(attempt);

        return toDTO(entity);
    }

    public SubmissionDTO submitOpenText(String email, UUID attemptId, String answerText) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        AssessmentAttemptEntity attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", "id", attemptId));

        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Este intento no te pertenece");
        }
        if (!"in_progress".equals(attempt.getStatus())) {
            throw new BadRequestException("Este intento ya fue enviado");
        }

        AssessmentEntity assessment = attempt.getAssessment();
        if (!"open_text".equals(assessment.getType())) {
            throw new BadRequestException("Esta evaluación no es de tipo respuesta abierta");
        }
        if (answerText == null || answerText.isBlank()) {
            throw new BadRequestException("La respuesta no puede estar vacia");
        }

        SubmissionEntity entity = submissionRepository.findByAttemptId(attemptId).orElseGet(SubmissionEntity::new);
        entity.setAttempt(attempt);
        entity.setType("open_text");
        entity.setAnswerText(answerText);
        entity = submissionRepository.save(entity);

        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setStatus("submitted");
        attemptRepository.save(attempt);

        return toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<SubmissionDTO> findPendingByAssessment(UUID assessmentId) {
        return submissionRepository.findPendingByAssessmentId(assessmentId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public String getDownloadUrl(UUID submissionId) {
        SubmissionEntity entity = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));
        if (entity.getStorageKey() == null) {
            throw new BadRequestException("Esta entrega no tiene archivo");
        }
        return storage.getPresignedUrl(entity.getStorageKey());
    }

    public SubmissionDTO grade(String graderEmail, UUID submissionId, int score, String feedback) {
        UserEntity grader = userRepository.findByEmail(graderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", graderEmail));

        SubmissionEntity entity = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        AssessmentAttemptEntity attempt = entity.getAttempt();
        AssessmentEntity assessment = attempt.getAssessment();

        if (score < 0 || score > 100) {
            throw new BadRequestException("La puntuación debe estar entre 0 y 100");
        }

        boolean passed = score >= assessment.getPassingScore();
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setStatus("graded");
        attemptRepository.save(attempt);

        entity.setInstructorFeedback(feedback);
        entity.setGradedAt(LocalDateTime.now());
        entity.setGradedBy(grader);
        entity = submissionRepository.save(entity);

        if (passed) {
            progressService.markCompleted(attempt.getUser().getId(), assessment.getLesson().getId());
        }

        return toDTO(entity);
    }

    @Transactional(readOnly = true)
    public SubmissionDTO findByAttempt(UUID attemptId) {
        return submissionRepository.findByAttemptId(attemptId).map(this::toDTO).orElse(null);
    }

    private String sanitize(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private SubmissionDTO toDTO(SubmissionEntity e) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(e.getId());
        dto.setAttemptId(e.getAttempt().getId());
        dto.setType(e.getType());
        dto.setFileName(e.getFileName());
        dto.setFileSize(e.getFileSize());
        dto.setAnswerText(e.getAnswerText());
        dto.setInstructorFeedback(e.getInstructorFeedback());
        dto.setGradedAt(e.getGradedAt());
        if (e.getGradedBy() != null) dto.setGradedById(e.getGradedBy().getId());
        dto.setCreatedAt(e.getCreatedAt());
        UserEntity student = e.getAttempt().getUser();
        if (student != null) {
            dto.setStudentId(student.getId());
            dto.setStudentName((student.getFirstName() + " " + student.getLastName()).trim());
        }
        return dto;
    }
}
