package com.example.subscriptionservice.subscription.dto;

import com.example.subscriptionservice.subscription.SubscriptionType;

import java.math.BigDecimal;
import java.time.LocalDate;


public record InvoiceEvent(
        Long userId,
        Long invoiceId,
        LocalDate issueDate,
        BigDecimal amount,
        SubscriptionType subscriptionType,
        LocalDate activationDate
) {}