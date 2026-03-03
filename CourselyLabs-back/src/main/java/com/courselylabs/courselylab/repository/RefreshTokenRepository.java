package com.courselylabs.courselylab.repository;

import com.courselylabs.courselylab.entity.RefreshTokenEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByToken(String token);

    void deleteByUser(UserEntity user);

    void deleteByToken(String token);
}
