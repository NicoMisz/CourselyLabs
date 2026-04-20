package com.courselylabs.courselylab.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.courselylabs.courselylab.entity.SubscriptionEntity;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {

    Optional<SubscriptionEntity> findByUserIdAndStatus(UUID userId, String status);

    Optional<SubscriptionEntity> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<SubscriptionEntity> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndStatus(UUID userId, String status);
}
