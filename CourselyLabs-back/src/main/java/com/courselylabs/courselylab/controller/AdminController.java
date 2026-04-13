package com.courselylabs.courselylab.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.CourseDTO;
import com.courselylabs.courselylab.dto.UserDTO;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminService.AdminStats> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<UserDTO> page = adminService.getUsers(search, role, isActive, pageable)
                .map(this::toUserDTO);
        return ResponseEntity.ok(page);
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserDTO> changeRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        UserEntity user = adminService.changeRole(id, body.get("role"));
        return ResponseEntity.ok(toUserDTO(user));
    }

    @PatchMapping("/users/{id}/ban")
    public ResponseEntity<UserDTO> banUser(@PathVariable UUID id) {
        return ResponseEntity.ok(toUserDTO(adminService.banUser(id)));
    }

    @PatchMapping("/users/{id}/unban")
    public ResponseEntity<UserDTO> unbanUser(@PathVariable UUID id) {
        return ResponseEntity.ok(toUserDTO(adminService.unbanUser(id)));
    }

    @GetMapping("/courses/pending")
    public ResponseEntity<List<CourseDTO>> getPendingCourses() {
        return ResponseEntity.ok(adminService.getPendingCourses());
    }

    @PatchMapping("/courses/{id}/approve")
    public ResponseEntity<CourseDTO> approveCourse(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.approveCourse(id));
    }

    @PatchMapping("/courses/{id}/reject")
    public ResponseEntity<CourseDTO> rejectCourse(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.rejectCourse(id, body.get("reason")));
    }

    private UserDTO toUserDTO(UserEntity u) {
        return new UserDTO(
            u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
            u.getRole(), u.getBio(), u.getProfilePictureUrl(),
            u.getIsVerified(), u.getIsActive(), u.getCreatedAt(), u.getUpdatedAt()
        );
    }
}
