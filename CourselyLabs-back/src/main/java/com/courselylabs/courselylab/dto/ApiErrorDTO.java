package com.courselylabs.courselylab.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorDTO {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String errorCode;
    private String message;
}