package com.courselylabs.courselylab.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReorderRequestDTO {

    @NotNull
    private List<ReorderItem> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReorderItem {
        @NotNull
        private UUID id;
        @NotNull
        private Integer position;
    }
}
