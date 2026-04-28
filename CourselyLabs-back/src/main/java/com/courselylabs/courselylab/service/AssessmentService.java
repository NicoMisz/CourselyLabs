package com.courselylabs.courselylab.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.AssessmentDTO;
import com.courselylabs.courselylab.dto.QuizOptionDTO;
import com.courselylabs.courselylab.dto.QuizQuestionDTO;
import com.courselylabs.courselylab.entity.AssessmentEntity;
import com.courselylabs.courselylab.entity.LessonBlockEntity;
import com.courselylabs.courselylab.entity.QuizOptionEntity;
import com.courselylabs.courselylab.entity.QuizQuestionEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.exception.UnauthorizedException;
import com.courselylabs.courselylab.repository.AssessmentRepository;
import com.courselylabs.courselylab.repository.LessonBlockRepository;
import com.courselylabs.courselylab.repository.QuizOptionRepository;
import com.courselylabs.courselylab.repository.QuizQuestionRepository;
import com.courselylabs.courselylab.repository.SubscriptionRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional
public class AssessmentService {

    private static final int FREE_LIMIT_PER_TYPE = 2;

    private final AssessmentRepository assessmentRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final LessonBlockRepository blockRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             QuizQuestionRepository questionRepository,
                             QuizOptionRepository optionRepository,
                             LessonBlockRepository blockRepository,
                             UserRepository userRepository,
                             SubscriptionRepository subscriptionRepository) {
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.blockRepository = blockRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public AssessmentDTO findByBlockId(UUID blockId, boolean includeAnswers) {
        AssessmentEntity entity = assessmentRepository.findByBlockId(blockId).orElse(null);
        if (entity == null) return null;
        return toDTO(entity, includeAnswers);
    }

    public AssessmentDTO createForBlock(UUID blockId, AssessmentDTO dto, String email) {
        LessonBlockEntity block = blockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", blockId));

        if (!LessonBlockService.isAssessmentType(block.getType())) {
            throw new BadRequestException(
                    "El bloque debe ser de tipo quiz, project u open_text para tener una evaluación");
        }

        if (assessmentRepository.findByBlockId(blockId).isPresent()) {
            throw new BadRequestException("Este bloque ya tiene una evaluación asociada");
        }

        String type = block.getType();
        UUID courseId = block.getLesson().getSection().getCourse().getId();
        assertCanCreate(email, courseId, type);

        AssessmentEntity entity = new AssessmentEntity();
        entity.setBlock(block);
        entity.setLesson(block.getLesson());
        entity.setType(type);
        entity.setDescription(dto.getDescription());
        entity.setMaxAttempts(dto.getMaxAttempts() != null ? dto.getMaxAttempts() : 3);
        entity.setTimeLimitMinutes(dto.getTimeLimitMinutes());
        entity.setPassingScore(dto.getPassingScore() != null ? dto.getPassingScore() : 70);
        entity.setShuffleOptions(dto.getShuffleOptions() != null ? dto.getShuffleOptions() : true);

        return toDTO(assessmentRepository.save(entity), true);
    }

    public AssessmentDTO update(UUID assessmentId, AssessmentDTO dto) {
        AssessmentEntity entity = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", assessmentId));

        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getMaxAttempts() != null) entity.setMaxAttempts(dto.getMaxAttempts());
        entity.setTimeLimitMinutes(dto.getTimeLimitMinutes());
        if (dto.getPassingScore() != null) entity.setPassingScore(dto.getPassingScore());
        if (dto.getShuffleOptions() != null) entity.setShuffleOptions(dto.getShuffleOptions());

        return toDTO(assessmentRepository.save(entity), true);
    }

    public void delete(UUID assessmentId) {
        if (!assessmentRepository.existsById(assessmentId)) {
            throw new ResourceNotFoundException("Assessment", "id", assessmentId);
        }
        assessmentRepository.deleteById(assessmentId);
    }

    // --- Questions ---

    public QuizQuestionDTO addQuestion(UUID assessmentId, QuizQuestionDTO dto) {
        AssessmentEntity assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", assessmentId));

        if (!"quiz".equals(assessment.getType())) {
            throw new BadRequestException("Solo los quizzes tienen preguntas");
        }

        QuizQuestionEntity question = new QuizQuestionEntity();
        question.setAssessment(assessment);
        question.setQuestionText(dto.getQuestionText());
        question.setPosition(dto.getPosition() != null ? dto.getPosition() : 0);
        question.setPoints(dto.getPoints() != null ? dto.getPoints() : 1);
        question = questionRepository.save(question);

        if (dto.getOptions() != null) {
            for (int i = 0; i < dto.getOptions().size(); i++) {
                QuizOptionDTO optDto = dto.getOptions().get(i);
                QuizOptionEntity opt = new QuizOptionEntity();
                opt.setQuestion(question);
                opt.setOptionText(optDto.getOptionText());
                opt.setIsCorrect(Boolean.TRUE.equals(optDto.getIsCorrect()));
                opt.setExplanation(optDto.getExplanation());
                opt.setPosition(optDto.getPosition() != null ? optDto.getPosition() : i);
                optionRepository.save(opt);
            }
        }

        return toQuestionDTO(question, true);
    }

    public QuizQuestionDTO updateQuestion(UUID questionId, QuizQuestionDTO dto) {
        QuizQuestionEntity question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        if (dto.getQuestionText() != null) question.setQuestionText(dto.getQuestionText());
        if (dto.getPosition() != null) question.setPosition(dto.getPosition());
        if (dto.getPoints() != null) question.setPoints(dto.getPoints());
        question = questionRepository.save(question);

        if (dto.getOptions() != null) {
            optionRepository.deleteByQuestionId(questionId);
            for (int i = 0; i < dto.getOptions().size(); i++) {
                QuizOptionDTO optDto = dto.getOptions().get(i);
                QuizOptionEntity opt = new QuizOptionEntity();
                opt.setQuestion(question);
                opt.setOptionText(optDto.getOptionText());
                opt.setIsCorrect(Boolean.TRUE.equals(optDto.getIsCorrect()));
                opt.setExplanation(optDto.getExplanation());
                opt.setPosition(optDto.getPosition() != null ? optDto.getPosition() : i);
                optionRepository.save(opt);
            }
        }

        return toQuestionDTO(question, true);
    }

    public void deleteQuestion(UUID questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question", "id", questionId);
        }
        questionRepository.deleteById(questionId);
    }

    // --- Limit checks ---

    public AssessmentEntity requireById(UUID id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", id));
    }

    private void assertCanCreate(String email, UUID courseId, String type) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String role = user.getRole() == null ? "user" : user.getRole();
        if ("admin".equals(role)) return;

        boolean isPremium = subscriptionRepository.existsByUserIdAndStatus(user.getId(), "active");
        if (isPremium) return;

        long count = assessmentRepository.findByCourseIdAndType(courseId, type).size();
        if (count >= FREE_LIMIT_PER_TYPE) {
            throw new UnauthorizedException(
                "Plan gratuito: límite de " + FREE_LIMIT_PER_TYPE + " evaluaciones de tipo '" + type + "' por curso. "
                + "Hazte Premium para crear sin límite.");
        }
    }

    // --- Mappers ---

    AssessmentDTO toDTO(AssessmentEntity entity, boolean includeAnswers) {
        AssessmentDTO dto = new AssessmentDTO();
        dto.setId(entity.getId());
        if (entity.getLesson() != null) dto.setLessonId(entity.getLesson().getId());
        if (entity.getBlock() != null) dto.setBlockId(entity.getBlock().getId());
        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setMaxAttempts(entity.getMaxAttempts());
        dto.setTimeLimitMinutes(entity.getTimeLimitMinutes());
        dto.setPassingScore(entity.getPassingScore());
        dto.setShuffleOptions(entity.getShuffleOptions());

        if ("quiz".equals(entity.getType())) {
            List<QuizQuestionEntity> questions = questionRepository.findByAssessmentIdOrderByPositionAsc(entity.getId());
            List<QuizQuestionDTO> qDTOs = new ArrayList<>();
            for (QuizQuestionEntity q : questions) {
                qDTOs.add(toQuestionDTO(q, includeAnswers));
            }
            dto.setQuestions(qDTOs);
        }

        return dto;
    }

    QuizQuestionDTO toQuestionDTO(QuizQuestionEntity entity, boolean includeAnswers) {
        QuizQuestionDTO dto = new QuizQuestionDTO();
        dto.setId(entity.getId());
        dto.setQuestionText(entity.getQuestionText());
        dto.setPosition(entity.getPosition());
        dto.setPoints(entity.getPoints());

        List<QuizOptionEntity> options = optionRepository.findByQuestionIdOrderByPositionAsc(entity.getId());
        List<QuizOptionDTO> oDTOs = new ArrayList<>();
        for (QuizOptionEntity o : options) {
            QuizOptionDTO oDto = new QuizOptionDTO();
            oDto.setId(o.getId());
            oDto.setOptionText(o.getOptionText());
            oDto.setPosition(o.getPosition());
            if (includeAnswers) {
                oDto.setIsCorrect(o.getIsCorrect());
                oDto.setExplanation(o.getExplanation());
            }
            oDTOs.add(oDto);
        }
        dto.setOptions(oDTOs);
        return dto;
    }
}
