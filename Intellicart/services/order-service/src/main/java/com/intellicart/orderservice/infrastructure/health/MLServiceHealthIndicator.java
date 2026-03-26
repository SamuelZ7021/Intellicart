package com.intellicart.orderservice.infrastructure.health;

import com.intellicart.orderservice.client.RecommendationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MLServiceHealthIndicator implements HealthIndicator {
    private final RecommendationClient mlClient;

    @Override
    public Health health() {
        try{
            if (mlClient.ping()){
                return Health.up().withDetail("Service", "ML Service").build();
            }
            return Health.down().withDetail("Service", " ML Service gRPC timeout").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
