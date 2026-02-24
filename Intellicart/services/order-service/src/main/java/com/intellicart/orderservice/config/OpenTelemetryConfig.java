package com.intellicart.orderservice.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {

    @Bean
    public Tracer opentelemetryTracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("order-service");
    }
}
