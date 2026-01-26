package com.courselylabs.courselylab.dto;

import java.security.Timestamp;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaDTO {
    private Long id;

    @Nonnull
    private String name;

    @Nonnull    
    private String slug;

    private String description;

    @Nonnull
    private Timestamp createdAt;
}
