package com.courselylabs.courselylab.controller;

import com.courselylabs.courselylab.dto.UserCreateDTO;
import com.courselylabs.courselylab.dto.UserDTO;
import com.courselylabs.courselylab.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDTO>> findByRole(@PathVariable String role) {
        return ResponseEntity.ok(userService.findByRole(role));
    }

    @GetMapping("/instructors")
    public ResponseEntity<List<UserDTO>> findInstructors() {
        return ResponseEntity.ok(userService.findInstructors());
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable UUID id, @Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserDTO> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deactivate(id));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<UserDTO> verify(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.verify(id));
    }
}
