package com.intellicart.userservice.domain.payment;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final MeterRegistry meterRegistry;

    public boolean processPayment(Long userId, Long orderId, BigDecimal amount) {
        log.info("Processing payment for order: {} user: {} amount: {}", orderId, userId, amount);
        
        Timer.Sample sample = Timer.start(meterRegistry);
        boolean result = false;

        try {
            // Logic segregated here. Still a mock for now, but architecturally cleaner.
            // In real world, this would call external payment gateway (Stripe/PayPal)
            boolean success = amount.doubleValue() < 10000;
            
            if (success) {
                log.info("Payment approved for order {}", orderId);
            } else {
                log.warn("Payment rejected for order {}", orderId);
            }
            result = success;
            return success;
        } finally {
           sample.stop(Timer.builder("payment.processed")
                   .tag("success", String.valueOf(result))
                   .register(meterRegistry));
        }
    }
}
