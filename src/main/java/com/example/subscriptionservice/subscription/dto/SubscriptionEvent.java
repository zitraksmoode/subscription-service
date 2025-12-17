package com.example.subscriptionservice.subscription.dto;

import com.example.subscriptionservice.subscription.SubscriptionType;

import java.time.LocalDate;

/**
 * Событие изменения подписки (активация или деактивация).
 */
public record SubscriptionEvent(
        Long userId,
        SubscriptionType type,
        LocalDate activationDate
) {}