package com.example.subscriptionservice.subscription.dto;

import jakarta.validation.constraints.NotNull;

public record DeactivateSubscriptionRequest(
        @NotNull(message = "userId обязателен")
        Long userId
) {}