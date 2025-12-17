package com.example.subscriptionservice.cache.listener;

import com.example.subscriptionservice.cache.dto.UserCache;
import com.example.subscriptionservice.subscription.dto.InvoiceEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Слушатель событий о новых счетах.
 * Обновляет кэш пользователя в Redis.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CACHE_KEY_PREFIX = "user:";

    @RabbitListener(queues = "invoices.queue")
    public void handleInvoiceEvent(String jsonMessage) {
        try {
            InvoiceEvent event = objectMapper.readValue(jsonMessage, InvoiceEvent.class);
            Long userId = event.userId();

            log.info("Получено событие о счёте для пользователя {}", userId);

            String cacheKey = CACHE_KEY_PREFIX + userId;

            // Получаем текущий кэш или создаём новый
            UserCache currentCache = (UserCache) redisTemplate.opsForValue().get(cacheKey);
            if (currentCache == null) {
                currentCache = new UserCache(null, new ArrayList<>());
            }

            // Обновляем список счетов (добавляем новый в начало — свежий сверху)
            // Обновляем список счетов (добавляем новый в начало — свежий сверху)
            var updatedInvoices = new ArrayList<>(currentCache.invoices());
            updatedInvoices.add(0, new UserCache.InvoiceCache(
                    event.invoiceId(),
                    event.issueDate(),
                    event.subscriptionType(),
                    event.activationDate()
            ));

// Для простоты пагинации — храним только последние 20 счетов
            List<UserCache.InvoiceCache> limitedInvoices;
            if (updatedInvoices.size() > 20) {
                limitedInvoices = new ArrayList<>(updatedInvoices.subList(0, 20));
            } else {
                limitedInvoices = updatedInvoices;
            }

// Обновляем кэш
            UserCache updatedCache = new UserCache(currentCache.activeSubscription(), limitedInvoices);
            redisTemplate.opsForValue().set(cacheKey, updatedCache);

            log.info("Кэш обновлён для пользователя {}", userId);

        } catch (Exception e) {
            log.error("Ошибка обработки события из RabbitMQ", e);
            // По ТЗ — предусмотрели недоступность Redis: просто логируем, fallback будет в API
        }
    }
}