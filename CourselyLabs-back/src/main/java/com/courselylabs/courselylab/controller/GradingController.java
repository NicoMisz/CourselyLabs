package com.courselylabs.courselylab.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.PendingSubmissionDTO;
import com.courselylabs.courselylab.service.GradingService;

@RestController
@RequestMapping("/api/grading")
public class GradingController {

    private final GradingService gradingService;

    public GradingController(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    /** All pending submissions across courses where the caller is owner/instructor (admin sees all). */
    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PendingSubmissionDTO>> findPending(Authentication auth) {
        return ResponseEntity.ok(gradingService.findPendingForUser(auth.getName()));
    }
}
