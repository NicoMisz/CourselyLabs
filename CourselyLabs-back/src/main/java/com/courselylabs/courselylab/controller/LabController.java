package com.courselylabs.courselylab.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.lab.EchoConnectionStatusDTO;
import com.courselylabs.courselylab.dto.lab.EchoTokenInputDTO;
import com.courselylabs.courselylab.dto.lab.LabConsoleDTO;
import com.courselylabs.courselylab.dto.lab.LabStatusDTO;
import com.courselylabs.courselylab.service.LabService;

import jakarta.validation.Valid;

/**
 * Endpoints de la integración echo lab.
 *
 * - {@code /api/me/echo}      → conexión del token personal del usuario.
 * - {@code /api/labs/{block}} → operar el laboratorio asociado a un bloque.
 *
 * Todos requieren autenticación (configurada en SecurityConfig).
 */
@RestController
@RequestMapping("/api")
public class LabController {

    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    // ── Conexión del token de echo ─────────────────────────────────

    @GetMapping("/me/echo")
    public ResponseEntity<EchoConnectionStatusDTO> getEchoStatus(Authentication auth) {
        return ResponseEntity.ok(labService.getConnectionStatus(auth.getName()));
    }

    @PutMapping("/me/echo")
    public ResponseEntity<EchoConnectionStatusDTO> connectEcho(
            @Valid @RequestBody EchoTokenInputDTO body,
            Authentication auth) {
        return ResponseEntity.ok(labService.connectToken(auth.getName(), body.getToken()));
    }

    @DeleteMapping("/me/echo")
    public ResponseEntity<Void> disconnectEcho(Authentication auth) {
        labService.disconnectToken(auth.getName());
        return ResponseEntity.noContent().build();
    }

    // ── Operar el laboratorio de un bloque ─────────────────────────

    @GetMapping("/labs/{blockId}/status")
    public ResponseEntity<LabStatusDTO> status(@PathVariable UUID blockId, Authentication auth) {
        return ResponseEntity.ok(labService.getStatus(auth.getName(), blockId));
    }

    @PostMapping("/labs/{blockId}/start")
    public ResponseEntity<LabStatusDTO> start(@PathVariable UUID blockId, Authentication auth) {
        return ResponseEntity.ok(labService.start(auth.getName(), blockId));
    }

    @PostMapping("/labs/{blockId}/stop")
    public ResponseEntity<LabStatusDTO> stop(@PathVariable UUID blockId, Authentication auth) {
        return ResponseEntity.ok(labService.stop(auth.getName(), blockId));
    }

    @PostMapping("/labs/{blockId}/console")
    public ResponseEntity<LabConsoleDTO> console(@PathVariable UUID blockId, Authentication auth) {
        return ResponseEntity.ok(labService.openConsole(auth.getName(), blockId));
    }
}
