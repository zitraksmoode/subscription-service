package com.example.subscriptionservice.subscription.dto;

import com.example.subscriptionservice.subscription.SubscriptionType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ActivateSubscriptionRequest(
        @NotNull(message = "userId обязателен")
        Long userId,

        @NotNull(message = "type обязателен")
        SubscriptionType type,

        @NotNull(message = "activationDate обязательна")
        @FutureOrPresent(message = "Дата активации не может быть в прошлом")
        LocalDate activationDate
) {}