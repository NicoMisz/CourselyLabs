package com.courselylabs.courselylab.repository;

import com.courselylabs.courselylab.entity.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Integer> {

    Optional<CategoriaEntity> findBySlug(String slug);

    Optional<CategoriaEntity> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);
}
