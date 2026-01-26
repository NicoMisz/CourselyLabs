package com.courselylabs.courselylab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaDTO {

    private Integer id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 100, message = "Slug must be less than 100 characters")
    private String slug;

    private String description;

    private LocalDateTime createdAt;
}
