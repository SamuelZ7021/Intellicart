package com.intellicart.userservice.domain.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class PaymentService {

    public boolean processPayment(Long userId, Long orderId, BigDecimal amount) {
        log.info("Processing payment for order: {} user: {} amount: {}", orderId, userId, amount);
        
        // Logic segregated here. Still a mock for now, but architecturally cleaner.
        // In real world, this would call external payment gateway (Stripe/PayPal)
        boolean success = amount.doubleValue() < 10000;
        
        if (success) {
            log.info("Payment approved for order {}", orderId);
        } else {
            log.warn("Payment rejected for order {}", orderId);
        }
        
        return success;
    }
}
