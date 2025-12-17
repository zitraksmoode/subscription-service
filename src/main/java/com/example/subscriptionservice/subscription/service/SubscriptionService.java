package com.example.subscriptionservice.subscription.service;

import com.example.subscriptionservice.subscription.SubscriptionType;
import com.example.subscriptionservice.subscription.dto.SubscriptionEvent;
import com.example.subscriptionservice.subscription.entity.Subscription;
import com.example.subscriptionservice.subscription.entity.User;
import com.example.subscriptionservice.subscription.repository.SubscriptionRepository;
import com.example.subscriptionservice.subscription.repository.UserRepository;
import com.example.subscriptionservice.subscription.RabbitConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void activate(Long userId, SubscriptionType type, LocalDate activationDate) {
        // Проверка: дата не в прошлом
        if (activationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Дата активации не может быть в прошлом");
        }

        userRepository.findById(userId).orElseGet(() -> userRepository.save(new User(userId)));
        subscriptionRepository.findByUserIdAndActiveTrue(userId)
                .ifPresent(oldSub -> {
                    oldSub.setActive(false);
                    subscriptionRepository.save(oldSub);

                    // Отправляем событие деактивации
                    sendSubscriptionEvent(userId, null, null);
                });

        Subscription newSub = new Subscription(userId, type, activationDate);
        subscriptionRepository.save(newSub);

        sendSubscriptionEvent(userId, type, activationDate);

        log.info("Подписка {} активирована для пользователя {}", type, userId);
    }

    public void deactivate(Long userId) {
        subscriptionRepository.findByUserIdAndActiveTrue(userId)
                .ifPresent(sub -> {
                    sub.setActive(false);
                    subscriptionRepository.save(sub);

                    // Отправляем событие деактивации
                    sendSubscriptionEvent(userId, null, null);

                    log.info("Подписка деактивирована для пользователя {}", userId);
                });
    }

    private void sendSubscriptionEvent(Long userId, SubscriptionType type, LocalDate activationDate) {
        SubscriptionEvent event = new SubscriptionEvent(userId, type, activationDate);
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(RabbitConfig.SUBSCRIPTIONS_QUEUE, json);
            log.info("Отправлено событие подписки в RabbitMQ для пользователя {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации события подписки", e);
        } catch (Exception e) {
            log.error("Ошибка отправки события подписки в RabbitMQ (брокер недоступен?)", e);
        }
    }
}