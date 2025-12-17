package com.example.subscriptionservice.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import com.example.subscriptionservice.subscription.SubscriptionType;

import java.time.LocalDate;

@Entity
@Table(name = "subscriptions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "active"}))
@Getter
@Setter
@NoArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type;

    @Column(name = "activation_date", nullable = false)
    private LocalDate activationDate;

    @Column(nullable = false)
    private boolean active = true;

    public Subscription(Long userId, SubscriptionType type, LocalDate activationDate) {
        this.userId = userId;
        this.type = type;
        this.activationDate = activationDate;
        this.active = true;
    }
}