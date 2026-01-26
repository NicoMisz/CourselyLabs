package com.courselylabs.courselylab.repository;

import com.courselylabs.courselylab.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntity> findByRole(String role);

    List<UserEntity> findByIsActiveTrue();

    List<UserEntity> findByRoleAndIsActiveTrue(String role);
}
