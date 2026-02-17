package com.intellicart.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendOrderConfirmation(Long userId, Long orderId) {
        log.info("Sending order confirmation email to user {} for order {}", userId, orderId);
        // Simulate email sending delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Order confirmation email sent successfully to user {}", userId);
    }

    public void sendPaymentConfirmation(Long orderId) {
        log.info("Sending payment success email for order {}", orderId);
    }

    public void sendPaymentFailure(Long orderId) {
        log.info("Sending payment failure email for order {}", orderId);
    }
}
