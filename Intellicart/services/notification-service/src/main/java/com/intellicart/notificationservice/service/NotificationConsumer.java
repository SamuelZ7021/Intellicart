package com.intellicart.notificationservice.service;

import com.intellicart.notificationservice.event.OrderCreatedEvent;
import com.intellicart.notificationservice.event.PaymentProcessedEvent;
import com.intellicart.notificationservice.event.UserCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;
    private final RealTimeNotificationService realTimeNotificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void handleUserCreated(String message) {
        log.info("Received user creation event: {}", message);
        try {
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);
            emailService.sendWelcomeEmail(event.getEmail(), event.getFirstName());
            realTimeNotificationService.sendToUser(event.getId(), "welcome", Map.of(
                "message", "Welcome to Intellicart, " + event.getFirstName() + "!"
            ));
        } catch (JsonProcessingException e) {
            log.error("Error parsing user creation event", e);
        }
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderCreated(String message) {
        log.info("Received order event: {}", message);
        try {
            if (message.contains("totalAmount")) {
                OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
                if (event.getUserEmail() != null) {
                    emailService.sendOrderConfirmation(event.getUserEmail(), event.getOrderId());
                }
                realTimeNotificationService.sendToUser(event.getUserId(), "order_created", Map.of(
                    "orderId", event.getOrderId(),
                    "status", "PENDING"
                ));
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
            
            // Send Email
            if (event.getUserEmail() != null) {
                if (event.isSuccess()) {
                    emailService.sendPaymentConfirmation(event.getUserEmail(), event.getOrderId());
                } else {
                    emailService.sendPaymentFailure(event.getUserEmail(), event.getOrderId());
                }
            }

            // Real-time notification
            realTimeNotificationService.sendToUser(event.getUserId(), "payment_processed", Map.of(
                "orderId", event.getOrderId(),
                "success", event.isSuccess(),
                "message", event.isSuccess() ? "Payment successful!" : "Payment failed."
            ));

        } catch (JsonProcessingException e) {
            log.error("Error parsing payment event", e);
        }
    }
}
