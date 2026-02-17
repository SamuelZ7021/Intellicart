package com.intellicart.notificationservice.service;

import com.intellicart.notificationservice.event.OrderCreatedEvent;
import com.intellicart.notificationservice.event.PaymentProcessedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderCreated(String message) {
        log.info("Received order event: {}", message);
        try {
            // Check if it's an OrderCreatedEvent (simple check for now, ideally envelopes or headers)
            if (message.contains("totalAmount")) {
                OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
                emailService.sendOrderConfirmation(event.getUserId(), event.getOrderId());
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing order event", e);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void handlePaymentProcessed(String message) {
        log.info("Received payment event: {}", message);
        try {
            PaymentProcessedEvent event = objectMapper.readValue(message, PaymentProcessedEvent.class);
            if (event.isSuccess()) {
                emailService.sendPaymentConfirmation(event.getOrderId());
            } else {
                emailService.sendPaymentFailure(event.getOrderId());
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing payment event", e);
        }
    }
}
