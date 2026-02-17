package com.intellicart.userservice.service;

import com.intellicart.userservice.event.OrderCreatedEvent;
import com.intellicart.userservice.event.PaymentProcessedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "user-group")
    public void handleOrderCreated(String message) {
        log.info("Received order event: {}", message);
        try {
            if (message.contains("totalAmount")) {
                OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
                processPayment(event);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing order event", e);
        }
    }

    private void processPayment(OrderCreatedEvent event) {
        log.info("Processing payment for order: {} user: {} amount: {}", 
                 event.getOrderId(), event.getUserId(), event.getTotalAmount());
        
        // Mock payment logic - assume success if amount < 10000
        boolean success = event.getTotalAmount().doubleValue() < 10000;
        
        PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(event.getOrderId(), success);
        
        try {
            // In a real SAGA, we might save state here before publishing
            kafkaTemplate.send("payment-events", event.getOrderId().toString(), paymentEvent);
            log.info("Sent payment event for order: {} success: {}", event.getOrderId(), success);
        } catch (Exception e) {
            log.error("Error sending payment event", e);
        }
    }
}
