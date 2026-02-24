package com.intellicart.orderservice.service;

import com.intellicart.orderservice.client.RecommendationClient;
import com.intellicart.orderservice.dto.RecommendationDTO;
import com.intellicart.grpc.ml.ProductDetail;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationClient recommendationClient;

    @CircuitBreaker(name = "recommendationService", fallbackMethod = "getRecommendationsFallback")
    public List<RecommendationDTO> getRecommendations(String userId) {
        log.info("Fetching recommendations for user: {}", userId);
        List<ProductDetail> products = recommendationClient.getRecommendations(userId, 5);
        
        return products.stream()
                .map(p -> RecommendationDTO.builder()
                        .productId(p.getProductId())
                        .score(p.getScore())
                        .reason(p.getReason())
                        .build())
                .collect(Collectors.toList());
    }

    public List<RecommendationDTO> getRecommendationsFallback(String userId, Throwable t) {
        log.error("Fallback de recomendaciones para usuario {}: {}", userId, t.getMessage());
        return List.of(); // Devuelve lista vacía o recomendaciones genéricas de caché
    }
}
