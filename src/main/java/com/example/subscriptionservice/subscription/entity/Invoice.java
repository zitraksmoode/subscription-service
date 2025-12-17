package com.example.subscriptionservice.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import com.example.subscriptionservice.subscription.SubscriptionType;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false)
    private SubscriptionType subscriptionType;

    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    public Invoice(Long userId, Long subscriptionId, LocalDate issueDate,
                   SubscriptionType type, LocalDate activationDate) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.issueDate = issueDate;
        this.amount = type.getPrice();
        this.subscriptionType = type;
        this.activationDate = activationDate;
    }
}