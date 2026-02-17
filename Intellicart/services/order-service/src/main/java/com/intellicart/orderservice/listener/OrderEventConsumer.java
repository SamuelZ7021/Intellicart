package com.intellicart.orderservice.listener;

import com.intellicart.orderservice.event.PaymentProcessedEvent;
import com.intellicart.orderservice.model.Order;
import com.intellicart.orderservice.model.OrderStatus;
import com.intellicart.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    @Transactional
    public void handlePaymentProcessed(String message) {
        log.info("Received payment event: {}", message);
        try {
            PaymentProcessedEvent event = objectMapper.readValue(message, PaymentProcessedEvent.class);
            updateOrderStatus(event);
        } catch (JsonProcessingException e) {
            log.error("Error parsing payment event", e);
        }
    }

    private void updateOrderStatus(PaymentProcessedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
            if (event.isSuccess()) {
                order.setStatus(OrderStatus.CONFIRMED);
                log.info("Order {} confirmed", order.getId());
            } else {
                order.setStatus(OrderStatus.CANCELLED);
                log.info("Order {} cancelled due to payment failure", order.getId());
            }
            orderRepository.save(order);
        }, () -> log.warn("Order {} not found for payment event", event.getOrderId()));
    }
}
