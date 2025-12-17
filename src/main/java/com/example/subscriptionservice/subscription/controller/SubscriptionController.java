package com.example.subscriptionservice.subscription.controller;

import com.example.subscriptionservice.subscription.dto.ActivateSubscriptionRequest;
import com.example.subscriptionservice.subscription.dto.DeactivateSubscriptionRequest;
import com.example.subscriptionservice.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для управления подписками.
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/activate")
    public ResponseEntity<String> activate(@Valid @RequestBody ActivateSubscriptionRequest request) {
        subscriptionService.activate(
                request.userId(),
                request.type(),
                request.activationDate()
        );
        return ResponseEntity.ok("Подписка успешно активирована");
    }

    @PostMapping("/deactivate")
    public ResponseEntity<String> deactivate(@Valid @RequestBody DeactivateSubscriptionRequest request) {
        subscriptionService.deactivate(request.userId());
        return ResponseEntity.ok("Подписка успешно деактивирована");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}