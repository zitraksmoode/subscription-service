package com.example.subscriptionservice.subscription.repository;

import com.example.subscriptionservice.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий для подписок.
 * Добавляем кастомный метод для поиска активной подписки пользователя.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserIdAndActiveTrue(Long userId);
}