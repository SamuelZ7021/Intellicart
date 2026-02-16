package com.intellicart.orderservice.client;

import com.miecommerce.grpc.ml.RecommendationServiceGrpc;
import com.miecommerce.grpc.ml.RecommendationRequest;
import com.miecommerce.grpc.ml.RecommendationResponse;
import com.miecommerce.grpc.ml.ProductDetail;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class RecommendationClient {

    @GrpcClient("ml-service")
    private RecommendationServiceGrpc.RecommendationServiceBlockingStub recommendationServiceStub;

    public List<ProductDetail> getRecommendations(String userId, int maxResults) {
        try {
            RecommendationRequest request = RecommendationRequest.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            RecommendationResponse response = recommendationServiceStub.getPersonalizedProducts(request);
            return response.getProductsList();
        } catch (Exception e) {
            // Fallback or error handling
            // For now, return empty list on error to be resilient
            return Collections.emptyList();
        }
    }
}
