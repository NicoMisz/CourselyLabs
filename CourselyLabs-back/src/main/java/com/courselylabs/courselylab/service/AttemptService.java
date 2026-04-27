package com.courselylabs.courselylab.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.AssessmentAttemptDTO;
import com.courselylabs.courselylab.dto.AssessmentDTO;
import com.courselylabs.courselylab.dto.AssessmentResultDTO;
import com.courselylabs.courselylab.dto.QuizAnswerDTO;
import com.courselylabs.courselylab.dto.QuizOptionDTO;
import com.courselylabs.courselylab.dto.QuizQuestionDTO;
import com.courselylabs.courselylab.entity.AssessmentAttemptEntity;
import com.courselylabs.courselylab.entity.AssessmentEntity;
import com.courselylabs.courselylab.entity.QuizAnswerEntity;
import com.courselylabs.courselylab.entity.QuizOptionEntity;
import com.courselylabs.courselylab.entity.QuizQuestionEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.exception.UnauthorizedException;
import com.courselylabs.courselylab.repository.AssessmentAttemptRepository;
import com.courselylabs.courselylab.repository.AssessmentRepository;
import com.courselylabs.courselylab.repository.QuizAnswerRepository;
import com.courselylabs.courselylab.repository.QuizOptionRepository;
import com.courselylabs.courselylab.repository.QuizQuestionRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional
public class AttemptService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final QuizAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final AssessmentService assessmentService;
    private final LessonProgressService progressService;

    public AttemptService(AssessmentRepository assessmentRepository,
                          AssessmentAttemptRepository attemptRepository,
                          QuizQuestionRepository questionRepository,
                          QuizOptionRepository optionRepository,
                          QuizAnswerRepository answerRepository,
                          UserRepository userRepository,
                          AssessmentService assessmentService,
                          LessonProgressService progressService) {
        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.assessmentService = assessmentService;
        this.progressService = progressService;
    }

    /**
     * Starts a new attempt for the given assessment.
     * Validates max attempts and creates the attempt record.
     * Returns the assessment with shuffled options if applicable (no isCorrect/explanation).
     */
    public StartedAttempt start(String email, UUID assessmentId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        AssessmentEntity assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", assessmentId));

        long usedAttempts = attemptRepository.countByAssessmentIdAndUserId(assessmentId, user.getId());
        if (usedAttempts >= assessment.getMaxAttempts()) {
            throw new BadRequestException("Has agotado todos los intentos disponibles");
        }

        AssessmentAttemptEntity attempt = new AssessmentAttemptEntity();
        attempt.setAssessment(assessment);
        attempt.setUser(user);
        attempt.setAttemptNumber((int) usedAttempts + 1);
        attempt.setStatus("in_progress");
        attempt = attemptRepository.save(attempt);

        AssessmentDTO dto = assessmentService.toDTO(assessment, false);
        if (Boolean.TRUE.equals(assessment.getShuffleOptions()) && "quiz".equals(assessment.getType())) {
            shuffleQuestionsAndOptions(dto, attempt.getId());
        }

        return new StartedAttempt(attempt.getId(), dto);
    }

    public record StartedAttempt(UUID attemptId, AssessmentDTO assessment) {}

    /**
     * Submits a quiz attempt with answers, autocorrects, marks lesson completed if passed.
     */
    public AssessmentResultDTO submitQuiz(String email, UUID attemptId, List<QuizAnswerDTO> answers) {
        AssessmentAttemptEntity attempt = requireOwnAttempt(email, attemptId);
        AssessmentEntity assessment = attempt.getAssessment();

        if (!"quiz".equals(assessment.getType())) {
            throw new BadRequestException("Esta evaluacion no es de tipo quiz");
        }
        if (!"in_progress".equals(attempt.getStatus())) {
            throw new BadRequestException("Este intento ya fue enviado");
        }

        // Time limit check
        if (assessment.getTimeLimitMinutes() != null) {
            LocalDateTime deadline = attempt.getStartedAt().plusMinutes(assessment.getTimeLimitMinutes());
            if (LocalDateTime.now().isAfter(deadline)) {
                attempt.setStatus("expired");
                attempt.setSubmittedAt(LocalDateTime.now());
                attempt.setScore(0);
                attempt.setPassed(false);
                attemptRepository.save(attempt);
                throw new BadRequestException("El tiempo se ha agotado. Intento marcado como expirado.");
            }
        }

        // Save answers
        answerRepository.deleteByAttemptId(attemptId);
        if (answers != null) {
            for (QuizAnswerDTO ans : answers) {
                if (ans.getQuestionId() == null) continue;
                QuizQuestionEntity q = questionRepository.findById(ans.getQuestionId()).orElse(null);
                if (q == null) continue;

                QuizAnswerEntity a = new QuizAnswerEntity();
                a.setAttempt(attempt);
                a.setQuestion(q);
                if (ans.getSelectedOptionId() != null) {
                    QuizOptionEntity opt = optionRepository.findById(ans.getSelectedOptionId()).orElse(null);
                    a.setSelectedOption(opt);
                }
                answerRepository.save(a);
            }
        }

        // Autocorrect
        List<QuizQuestionEntity> questions = questionRepository.findByAssessmentIdOrderByPositionAsc(assessment.getId());
        int totalPoints = questions.stream().mapToInt(q -> q.getPoints() == null ? 1 : q.getPoints()).sum();
        int earnedPoints = 0;

        Map<UUID, UUID> selectedByQuestion = new HashMap<>();
        for (QuizAnswerEntity a : answerRepository.findByAttemptId(attemptId)) {
            if (a.getSelectedOption() != null) {
                selectedByQuestion.put(a.getQuestion().getId(), a.getSelectedOption().getId());
            }
        }

        for (QuizQuestionEntity q : questions) {
            UUID selectedId = selectedByQuestion.get(q.getId());
            if (selectedId == null) continue;
            QuizOptionEntity selected = optionRepository.findById(selectedId).orElse(null);
            if (selected != null && Boolean.TRUE.equals(selected.getIsCorrect())) {
                earnedPoints += q.getPoints() == null ? 1 : q.getPoints();
            }
        }

        int scorePct = totalPoints > 0 ? (int) Math.round((earnedPoints * 100.0) / totalPoints) : 0;
        boolean passed = scorePct >= assessment.getPassingScore();

        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setScore(scorePct);
        attempt.setPassed(passed);
        attempt.setStatus("graded");
        attemptRepository.save(attempt);

        if (passed) {
            progressService.markCompleted(attempt.getUser().getId(), assessment.getLesson().getId());
        }

        return buildResult(attempt);
    }

    @Transactional(readOnly = true)
    public List<AssessmentAttemptDTO> findMyAttempts(String email, UUID assessmentId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return attemptRepository
                .findByAssessmentIdAndUserIdOrderByAttemptNumberDesc(assessmentId, user.getId())
                .stream()
                .map(this::toAttemptDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssessmentResultDTO getResult(String email, UUID attemptId) {
        AssessmentAttemptEntity attempt = requireOwnAttempt(email, attemptId);
        return buildResult(attempt);
    }

    AssessmentAttemptEntity requireOwnAttempt(String email, UUID attemptId) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        AssessmentAttemptEntity attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", "id", attemptId));
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Este intento no te pertenece");
        }
        return attempt;
    }

    private AssessmentResultDTO buildResult(AssessmentAttemptEntity attempt) {
        AssessmentEntity assessment = attempt.getAssessment();
        AssessmentResultDTO result = new AssessmentResultDTO();
        result.setAttemptId(attempt.getId());
        result.setScore(attempt.getScore());
        result.setPassed(attempt.getPassed());
        result.setPassingScore(assessment.getPassingScore());

        List<QuizQuestionEntity> questions = questionRepository.findByAssessmentIdOrderByPositionAsc(assessment.getId());
        result.setMaxScore(questions.stream().mapToInt(q -> q.getPoints() == null ? 1 : q.getPoints()).sum());

        List<QuizQuestionDTO> qDTOs = new ArrayList<>();
        for (QuizQuestionEntity q : questions) {
            qDTOs.add(assessmentService.toQuestionDTO(q, true));
        }
        result.setQuestions(qDTOs);

        // Map answers
        List<QuizAnswerDTO> answerDTOs = new ArrayList<>();
        for (QuizAnswerEntity a : answerRepository.findByAttemptId(attempt.getId())) {
            answerDTOs.add(new QuizAnswerDTO(
                    a.getQuestion().getId(),
                    a.getSelectedOption() != null ? a.getSelectedOption().getId() : null
            ));
        }
        result.setAnswers(answerDTOs);

        return result;
    }

    private AssessmentAttemptDTO toAttemptDTO(AssessmentAttemptEntity entity) {
        return new AssessmentAttemptDTO(
                entity.getId(),
                entity.getAssessment().getId(),
                entity.getStartedAt(),
                entity.getSubmittedAt(),
                entity.getScore(),
                entity.getPassed(),
                entity.getAttemptNumber(),
                entity.getStatus()
        );
    }

    private void shuffleQuestionsAndOptions(AssessmentDTO dto, UUID seed) {
        if (dto.getQuestions() == null) return;
        Random rng = new Random(seed.getMostSignificantBits() ^ seed.getLeastSignificantBits());
        for (QuizQuestionDTO q : dto.getQuestions()) {
            if (q.getOptions() == null) continue;
            List<QuizOptionDTO> shuffled = new ArrayList<>(q.getOptions());
            Collections.shuffle(shuffled, rng);
            q.setOptions(shuffled);
        }
    }
}
