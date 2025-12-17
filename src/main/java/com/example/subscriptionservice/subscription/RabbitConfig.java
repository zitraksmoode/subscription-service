package com.example.subscriptionservice.subscription;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация RabbitMQ.
 * Создаёт очередь, в которую будем отправлять события о новых счетах.
 */
@Configuration
public class RabbitConfig {

    public static final String INVOICES_QUEUE = "invoices.queue";
    public static final String SUBSCRIPTIONS_QUEUE = "subscriptions.queue";

    @Bean
    public Queue invoicesQueue() {
        return new Queue(INVOICES_QUEUE, true);
    }
    @Bean
    public Queue subscriptionsQueue() {
        return new Queue(SUBSCRIPTIONS_QUEUE, true);
    }
}