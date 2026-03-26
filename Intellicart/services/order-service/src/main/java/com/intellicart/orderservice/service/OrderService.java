package com.intellicart.orderservice.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellicart.orderservice.dto.OrderItemRequest;
import com.intellicart.orderservice.dto.OrderItemResponse;
import com.intellicart.orderservice.dto.OrderRequest;
import com.intellicart.orderservice.dto.OrderResponse;
import com.intellicart.orderservice.domain.model.Order;
import com.intellicart.orderservice.domain.model.OrderItem;
import com.intellicart.orderservice.domain.model.OrderStatus;
import com.intellicart.orderservice.domain.model.OutboxEvent;
import com.intellicart.orderservice.repository.OrderRepository;
import com.intellicart.orderservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.opentelemetry.api.trace.Span;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final io.micrometer.core.instrument.MeterRegistry meterRegistry;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        if (request.getUserId() == null) {
            throw new IllegalArgumentException("UserId cannot be null in order request.");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("An order must have at least one item.");
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> items = request.getItems().stream()
                .map(itemRequest -> {
                    if (itemRequest.getPrice() == null) {
                        throw new IllegalArgumentException("Price cannot be null for product: " + itemRequest.getProductName());
                    }
                    return mapToOrderItem(itemRequest, order);
                })
                .collect(Collectors.toList());
        
        order.setItems(items);

        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        String traceId = Span.current().getSpanContext().getTraceId();
        String spanId = Span.current().getSpanContext().getSpanId();

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("orderId", savedOrder.getId());
        payload.put("userId", savedOrder.getUserId());
        payload.put("userEmail", request.getUserEmail());
        payload.put("totalAmount", savedOrder.getTotalAmount());
        payload.put("_trace_context", traceId);

        // Inyectamos metadatos de trazabilidad para que el worker los use
        ObjectNode traceMetadata = payload.putObject("_trace_metadata");
        traceMetadata.put("trace_id", traceId);
        traceMetadata.put("span_id", spanId);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(savedOrder.getId().toString())
                .aggregateType("ORDER")
                .eventType("order-events")
                .payload(payload.toString())
                .createdAt(LocalDateTime.now())
                .processed(false)
                .build();


        outboxRepository.save(outboxEvent);

        meterRegistry.counter("orders.created").increment();

        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderItem mapToOrderItem(OrderItemRequest request, Order order) {
        return OrderItem.builder()
                .productId(request.getProductId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .order(order)
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
