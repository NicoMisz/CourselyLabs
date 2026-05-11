package com.courselylabs.courselylab.dto.lab;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EchoTokenInputDTO {
    @NotBlank(message = "El token es obligatorio")
    @Size(min = 32, max = 256, message = "Token con formato inesperado")
    private String token;
}
