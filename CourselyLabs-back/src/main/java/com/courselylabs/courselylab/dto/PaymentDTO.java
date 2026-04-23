package com.courselylabs.courselylab.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private String id;
    private String type;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
