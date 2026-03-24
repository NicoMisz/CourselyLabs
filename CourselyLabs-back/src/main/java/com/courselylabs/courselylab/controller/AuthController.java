package com.courselylabs.courselylab.controller;

import com.courselylabs.courselylab.dto.ChangePasswordDTO;
import com.courselylabs.courselylab.dto.auth.AuthResponseDTO;
import com.courselylabs.courselylab.dto.auth.LoginRequestDTO;
import com.courselylabs.courselylab.dto.auth.RefreshRequestDTO;
import com.courselylabs.courselylab.dto.auth.RegisterRequestDTO;
import com.courselylabs.courselylab.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO dto) {
        return ResponseEntity.ok(authService.refresh(dto.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequestDTO dto) {
        authService.logout(dto.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordDTO dto,
            Authentication auth) {
        authService.changePassword(auth.getName(), dto);
        return ResponseEntity.noContent().build();
    }
}
