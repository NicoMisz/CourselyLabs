package com.courselylabs.courselylab.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email no válido")
    private String email;
}
