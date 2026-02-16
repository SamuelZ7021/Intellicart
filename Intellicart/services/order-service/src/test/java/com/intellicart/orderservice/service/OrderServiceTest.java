package com.intellicart.orderservice.service;

import com.intellicart.orderservice.dto.OrderItemRequest;
import com.intellicart.orderservice.dto.OrderRequest;
import com.intellicart.orderservice.dto.OrderResponse;
import com.intellicart.orderservice.event.OrderCreatedEvent;
import com.intellicart.orderservice.model.Order;
import com.intellicart.orderservice.model.OrderItem;
import com.intellicart.orderservice.model.OrderStatus;
import com.intellicart.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_Success() {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setItems(List.of(new OrderItemRequest(1L, "Product A", 2, new BigDecimal("10.00"))));

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUserId(1L);
        savedOrder.setTotalAmount(new BigDecimal("20.00"));
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setItems(List.of(new OrderItem(1L, 1L, "Product A", 2, new BigDecimal("10.00"), savedOrder)));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(kafkaTemplate.send(anyString(), any(OrderCreatedEvent.class))).thenReturn(null);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("20.00"), response.getTotalAmount());
        verify(kafkaTemplate, times(1)).send(eq("order-events"), any(OrderCreatedEvent.class));
    }
}
