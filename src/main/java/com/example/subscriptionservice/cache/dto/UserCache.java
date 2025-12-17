package com.example.subscriptionservice.cache.dto;

import com.example.subscriptionservice.subscription.SubscriptionType;

import java.time.LocalDate;
import java.util.List;

/**
 * Данные пользователя в кэше Redis.
 * Храним активную подписку и список счетов (пагинированный).
 */
public record UserCache(
        ActiveSubscription activeSubscription,
        List<InvoiceCache> invoices
) {

    public record ActiveSubscription(
            SubscriptionType type,
            LocalDate activationDate
    ) {}

    public record InvoiceCache(
            Long invoiceId,
            LocalDate issueDate,
            SubscriptionType subscriptionType,
            LocalDate activationDate
    ) {}
}