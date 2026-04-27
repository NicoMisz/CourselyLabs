package com.courselylabs.courselylab.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.courselylabs.courselylab.dto.SubmissionDTO;
import com.courselylabs.courselylab.service.SubmissionService;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/attempts/{attemptId}/submit-project")
    @PreAuthorize("@courseSecurityService.canAccessAttempt(#attemptId, authentication)")
    public ResponseEntity<SubmissionDTO> submitProject(
            @PathVariable UUID attemptId,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        return ResponseEntity.ok(submissionService.submitProject(auth.getName(), attemptId, file));
    }

    @PostMapping("/attempts/{attemptId}/submit-open-text")
    @PreAuthorize("@courseSecurityService.canAccessAttempt(#attemptId, authentication)")
    public ResponseEntity<SubmissionDTO> submitOpenText(
            @PathVariable UUID attemptId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        return ResponseEntity.ok(submissionService.submitOpenText(auth.getName(), attemptId, body.get("answerText")));
    }

    @GetMapping("/attempts/{attemptId}/submission")
    @PreAuthorize("@courseSecurityService.canAccessAttempt(#attemptId, authentication)")
    public ResponseEntity<SubmissionDTO> findByAttempt(@PathVariable UUID attemptId) {
        SubmissionDTO dto = submissionService.findByAttempt(attemptId);
        return dto == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(dto);
    }

    @GetMapping("/assessments/{assessmentId}/submissions/pending")
    @PreAuthorize("@courseSecurityService.canEditAssessment(#assessmentId, authentication)")
    public ResponseEntity<List<SubmissionDTO>> findPending(@PathVariable UUID assessmentId) {
        return ResponseEntity.ok(submissionService.findPendingByAssessment(assessmentId));
    }

    @GetMapping("/submissions/{id}/download")
    @PreAuthorize("@courseSecurityService.canGradeSubmission(#id, authentication)")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("url", submissionService.getDownloadUrl(id)));
    }

    @PatchMapping("/submissions/{id}/grade")
    @PreAuthorize("@courseSecurityService.canGradeSubmission(#id, authentication)")
    public ResponseEntity<SubmissionDTO> grade(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        int score = ((Number) body.getOrDefault("score", 0)).intValue();
        String feedback = (String) body.get("feedback");
        return ResponseEntity.ok(submissionService.grade(auth.getName(), id, score, feedback));
    }
}
