package com.courselylabs.courselylab.repository.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.courselylabs.courselylab.entity.CourseEntity;

import jakarta.persistence.criteria.Predicate;

public final class CourseSpecifications {
    private CourseSpecifications() {}

    public static Specification<CourseEntity> publishedWithFilters(
        String keyword,
        Integer categoryId,
        String level,
        Boolean isFree,
        Double minRating
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("isPublished")));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (level != null && !level.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("level")), level.trim().toLowerCase()));
            }

            if (isFree != null) {
                predicates.add(cb.equal(root.get("isFree"), isFree));
            }

            if (minRating != null) {
                predicates.add(cb.ge(
                        root.get("averageRating").as(Double.class),
                        minRating
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}