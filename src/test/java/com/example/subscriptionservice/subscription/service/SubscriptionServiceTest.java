package com.example.subscriptionservice.subscription.service;

import com.example.subscriptionservice.subscription.SubscriptionType;
import com.example.subscriptionservice.subscription.entity.Subscription;
import com.example.subscriptionservice.subscription.entity.User;
import com.example.subscriptionservice.subscription.repository.SubscriptionRepository;
import com.example.subscriptionservice.subscription.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void activateNewSubscription_whenNoActive() {
        Long userId = 1L;
        LocalDate futureDate = LocalDate.now().plusDays(1);
        SubscriptionType type = SubscriptionType.PRO;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(subscriptionRepository.findByUserIdAndActiveTrue(userId)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

        subscriptionService.activate(userId, type, futureDate);

        verify(userRepository).save(any(User.class));
        verify(subscriptionRepository).save(argThat(sub ->
                sub.getUserId().equals(userId) &&
                        sub.getType() == type &&
                        sub.getActivationDate().equals(futureDate) &&
                        sub.isActive()
        ));
        verify(rabbitTemplate).convertAndSend(eq("subscriptions.queue"), anyString());
    }

    @Test
    void activateReplacesOldSubscription() {
        Long userId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);

        Subscription oldSub = new Subscription();
        oldSub.setActive(true);

        when(subscriptionRepository.findByUserIdAndActiveTrue(userId)).thenReturn(Optional.of(oldSub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

        subscriptionService.activate(userId, SubscriptionType.BASIC, date);

        assertFalse(oldSub.isActive());
        verify(subscriptionRepository, times(2)).save(any(Subscription.class)); // старая + новая
        verify(rabbitTemplate, times(2)).convertAndSend(eq("subscriptions.queue"), anyString()); // деактивация + активация
    }

    @Test
    void deactivateRemovesActiveSubscription() {
        Long userId = 1L;
        Subscription sub = new Subscription();
        sub.setActive(true);

        when(subscriptionRepository.findByUserIdAndActiveTrue(userId)).thenReturn(Optional.of(sub));

        subscriptionService.deactivate(userId);

        assertFalse(sub.isActive());
        verify(subscriptionRepository).save(sub);
        verify(rabbitTemplate).convertAndSend(eq("subscriptions.queue"), anyString());
    }

    @Test
    void activateThrowsIfDateInPast() {
        assertThrows(IllegalArgumentException.class, () ->
                subscriptionService.activate(1L, SubscriptionType.PRO, LocalDate.now().minusDays(1)));
    }
}