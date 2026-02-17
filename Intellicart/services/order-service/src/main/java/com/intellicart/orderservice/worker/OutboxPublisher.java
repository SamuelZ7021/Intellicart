package com.intellicart.orderservice.worker;

import com.intellicart.orderservice.model.OutboxEvent;
import com.intellicart.orderservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    @Transactional
    public void publishEvents() {
        List<OutboxEvent> unprocessedEvents = outboxRepository.findByProcessedFalse();

        if (unprocessedEvents.isEmpty()) {
            return;
        }

        log.info("Found {} unprocessed outbox events", unprocessedEvents.size());

        for (OutboxEvent event : unprocessedEvents) {
            try {
                String topic = determineTopic(event.getEventType());
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());
                
                event.setProcessed(true);
                event.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(event);
                
                log.info("Published event {} to topic {}", event.getId(), topic);
            } catch (Exception e) {
                log.error("Failed to publish event {}", event.getId(), e);
            }
        }
    }

    private String determineTopic(String eventType) {
        // Simple mapping, can be improved
        if (eventType.startsWith("ORDER")) {
            return "order-events";
        }
        return "default-events";
    }
}
