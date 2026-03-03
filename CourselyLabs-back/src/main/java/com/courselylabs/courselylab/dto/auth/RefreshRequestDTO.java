package com.courselylabs.courselylab.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequestDTO {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
