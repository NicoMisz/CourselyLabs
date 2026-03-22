package com.courselylabs.courselylab.service;

public enum CourseSearchSort {
    RECENT,
    POPULAR,
    RATING,
    PRICE_ASC,
    PRICE_DESC;

    public static CourseSearchSort fromQueryParam(String value) {
        if (value == null || value.isBlank()) {
            return RECENT;
        }

        return switch (value.toLowerCase()) {
            case "recent" -> RECENT;
            case "popular" -> POPULAR;
            case "rating" -> RATING;
            case "price_asc" -> PRICE_ASC;
            case "price_desc" -> PRICE_DESC;
            default -> throw new IllegalArgumentException("sortBy invalido: " + value);
        };
    }
}