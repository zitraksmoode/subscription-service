package com.example.subscriptionservice.subscription.scheduler;

import com.example.subscriptionservice.subscription.dto.InvoiceEvent;
import com.example.subscriptionservice.subscription.entity.Invoice;
import com.example.subscriptionservice.subscription.entity.Subscription;
import com.example.subscriptionservice.subscription.repository.InvoiceRepository;
import com.example.subscriptionservice.subscription.repository.SubscriptionRepository;
import com.example.subscriptionservice.subscription.RabbitConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(cron = "0 0 1 * * *")
    public void generateDailyInvoices() {
        LocalDate today = LocalDate.now();
        int dayOfMonth = today.getDayOfMonth();

        log.info("Запуск ежедневного биллинга. Сегодня {} число", dayOfMonth);

        subscriptionRepository.findAll().stream()
                .filter(sub -> sub.isActive())
                .filter(sub -> sub.getActivationDate().getDayOfMonth() == dayOfMonth)
                .forEach(sub -> {
                    Invoice invoice = new Invoice(
                            sub.getUserId(),
                            sub.getId(),
                            today,
                            sub.getType(),
                            sub.getActivationDate()
                    );
                    invoiceRepository.save(invoice);

                    InvoiceEvent event = new InvoiceEvent(
                            sub.getUserId(),
                            invoice.getId(),
                            today,
                            sub.getType().getPrice(),
                            sub.getType(),
                            sub.getActivationDate()
                    );

                    try {
                        String json = objectMapper.writeValueAsString(event);
                        rabbitTemplate.convertAndSend(RabbitConfig.INVOICES_QUEUE, json);
                        log.info("Отправлено событие в RabbitMQ: счёт {} для пользователя {}", invoice.getId(), sub.getUserId());
                    } catch (JsonProcessingException e) {
                        log.error("Ошибка сериализации JSON", e);
                    } catch (Exception e) {
                        log.error("Ошибка отправки в RabbitMQ", e);
                    }

                    log.info("Выставлен счёт для пользователя {} на сумму {} руб", sub.getUserId(), sub.getType().getPrice());
                });

        log.info("Биллинг завершён");
    }
}