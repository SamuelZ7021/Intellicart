package com.intellicart.orderservice.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellicart.orderservice.model.OutboxEvent;
import com.intellicart.orderservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.api.trace.Tracer;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Tracer tracer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    public void publishEvents() throws JsonProcessingException {
        List<OutboxEvent> unprocessedEvents = outboxRepository.findByProcessedFalse();

        for (OutboxEvent event : unprocessedEvents) {
            JsonNode payload = objectMapper.readTree(event.getPayload());
            String traceId = payload.get("_trace_metadata").get("trace_id").asText();
            String spanId = payload.get("_span_metadata").get("span_id").asText();

            io.opentelemetry.api.trace.SpanContext remoteContext = SpanContext.create(
                    traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault()
            );

            Span publishingSpan = tracer.spanBuilder("outbox_publishing_kafka")
                    .setParent(Context.current().with(Span.wrap(remoteContext)))
                    .startSpan();

            try (var scope = publishingSpan.makeCurrent()) {
                kafkaTemplate.send(event.getEventType(), event.getPayload());
                event.setProcessed(true);
                outboxRepository.save(event);
            } catch (Exception e) {
                publishingSpan.recordException(e);
                log.error("Error publishing event", e);
            } finally {
                publishingSpan.end();
            }
        }
    }
}
