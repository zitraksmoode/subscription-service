package com.example.subscriptionservice.cache.controller;

import com.example.subscriptionservice.cache.dto.UserCache;
import com.example.subscriptionservice.subscription.entity.Invoice;
import com.example.subscriptionservice.subscription.entity.Subscription;
import com.example.subscriptionservice.subscription.repository.InvoiceRepository;
import com.example.subscriptionservice.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Кэш-сервис API.
 * GET /api/cache/users/{userId}
 * Сначала из Redis, при недоступности — fallback на PostgreSQL.
 */
@RestController
@RequestMapping("/api/cache/users")
@RequiredArgsConstructor
@Slf4j
public class CacheController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    private static final String CACHE_KEY_PREFIX = "user:";

    @GetMapping("/{userId}")
    public ResponseEntity<UserCache> getUserData(@PathVariable Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;

        try {
            UserCache cached = (UserCache) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("Данные пользователя {} взяты из Redis", userId);
                return ResponseEntity.ok(cached);
            }
        } catch (Exception e) {
            log.warn("Redis недоступен — fallback на PostgreSQL для пользователя {}", userId, e);
        }

        Subscription activeSub = subscriptionRepository.findByUserIdAndActiveTrue(userId).orElse(null);

        UserCache.ActiveSubscription active = activeSub != null
                ? new UserCache.ActiveSubscription(activeSub.getType(), activeSub.getActivationDate())
                : null;

        List<Invoice> invoices = invoiceRepository.findByUserIdOrderByIssueDateDesc(userId);
        List<UserCache.InvoiceCache> invoiceCaches = invoices.stream()
                .map(inv -> new UserCache.InvoiceCache(
                        inv.getId(),
                        inv.getIssueDate(),
                        inv.getSubscriptionType(),
                        inv.getActivationDate()
                ))
                .limit(20)
                .toList();

        UserCache fallbackCache = new UserCache(active, invoiceCaches);
        log.info("Данные пользователя {} взяты из PostgreSQL (fallback)", userId);

        return ResponseEntity.ok(fallbackCache);
    }
}