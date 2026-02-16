package com.intellicart.orderservice.service;

import com.intellicart.orderservice.dto.OrderItemRequest;
import com.intellicart.orderservice.dto.OrderItemResponse;
import com.intellicart.orderservice.dto.OrderRequest;
import com.intellicart.orderservice.dto.OrderResponse;
import com.intellicart.orderservice.event.OrderCreatedEvent;
import com.intellicart.orderservice.model.Order;
import com.intellicart.orderservice.model.OrderItem;
import com.intellicart.orderservice.model.OrderStatus;
import com.intellicart.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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
        OrderCreatedEvent event = new OrderCreatedEvent(savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalAmount());
        kafkaTemplate.send("order-events", event);
        log.info("Order created with ID: {}", savedOrder.getId());

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
