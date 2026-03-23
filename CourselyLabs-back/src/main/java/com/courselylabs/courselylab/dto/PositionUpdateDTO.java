package com.courselylabs.courselylab.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionUpdateDTO {

    @NotNull
    @Min(0)
    private Integer positionSeconds;
}
