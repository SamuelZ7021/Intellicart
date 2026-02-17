package com.intellicart.orderservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEvent {
    @Id
    private UUID id;
    private  String aggregateId;
    private String aggregateType;
    private String eventType;
    @Column(columnDefinition = "jsonb")
    private String payload;
    private LocalDateTime createdAt;
    private boolean processed;
    private LocalDateTime processedAt;

}
