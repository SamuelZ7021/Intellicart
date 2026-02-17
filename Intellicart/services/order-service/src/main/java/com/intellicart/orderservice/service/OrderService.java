package com.intellicart.orderservice.service;

import com.intellicart.orderservice.dto.OrderItemRequest;
import com.intellicart.orderservice.dto.OrderItemResponse;
import com.intellicart.orderservice.dto.OrderRequest;
import com.intellicart.orderservice.dto.OrderResponse;
import com.intellicart.orderservice.event.OrderCreatedEvent;
import com.intellicart.orderservice.model.Order;
import com.intellicart.orderservice.model.OrderItem;
import com.intellicart.orderservice.model.OrderStatus;
import com.intellicart.orderservice.model.OutboxEvent;
import com.intellicart.orderservice.repository.OrderRepository;
import com.intellicart.orderservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> items = request.getItems().stream()
                .map(itemRequest -> mapToOrderItem(itemRequest, order))
                .collect(Collectors.toList());
        
        order.setItems(items);

        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        
        // Publish event
        String payload;
        try {
            payload = objectMapper.writeValueAsString(new OrderCreatedEvent(savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalAmount()));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Error creating order event payload", e);
        }

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(savedOrder.getId().toString())
                .aggregateType("ORDER")
                .eventType("ORDER_CREATED")
                .payload(payload)
                .createdAt(LocalDateTime.now())
                .processed(false)
                .build();

        
        outboxRepository.save(outboxEvent);

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
