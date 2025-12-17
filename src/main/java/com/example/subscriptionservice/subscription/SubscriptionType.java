package com.example.subscriptionservice.subscription;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum SubscriptionType {
    BASIC(new BigDecimal("100")),
    PRO(new BigDecimal("200"));

    private final BigDecimal price;

    SubscriptionType(BigDecimal price) {
        this.price = price;
    }
}
