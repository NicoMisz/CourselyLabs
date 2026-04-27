package com.courselylabs.courselylab.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.AssessmentAttemptDTO;
import com.courselylabs.courselylab.dto.AssessmentResultDTO;
import com.courselylabs.courselylab.dto.QuizAnswerDTO;
import com.courselylabs.courselylab.service.AttemptService;

@RestController
@RequestMapping("/api")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/assessments/{id}/start")
    @PreAuthorize("@courseSecurityService.canAccessAssessment(#id, authentication)")
    public ResponseEntity<AttemptService.StartedAttempt> start(
            @PathVariable UUID id,
            Authentication auth) {
        return ResponseEntity.ok(attemptService.start(auth.getName(), id));
    }

    @PostMapping("/attempts/{id}/submit-quiz")
    @PreAuthorize("@courseSecurityService.canAccessAttempt(#id, authentication)")
    public ResponseEntity<AssessmentResultDTO> submitQuiz(
            @PathVariable UUID id,
            @RequestBody List<QuizAnswerDTO> answers,
            Authentication auth) {
        return ResponseEntity.ok(attemptService.submitQuiz(auth.getName(), id, answers));
    }

    @GetMapping("/assessments/{id}/attempts")
    @PreAuthorize("@courseSecurityService.canAccessAssessment(#id, authentication)")
    public ResponseEntity<List<AssessmentAttemptDTO>> myAttempts(
            @PathVariable UUID id,
            Authentication auth) {
        return ResponseEntity.ok(attemptService.findMyAttempts(auth.getName(), id));
    }

    @GetMapping("/attempts/{id}/result")
    @PreAuthorize("@courseSecurityService.canAccessAttempt(#id, authentication)")
    public ResponseEntity<AssessmentResultDTO> getResult(
            @PathVariable UUID id,
            Authentication auth) {
        return ResponseEntity.ok(attemptService.getResult(auth.getName(), id));
    }
}
