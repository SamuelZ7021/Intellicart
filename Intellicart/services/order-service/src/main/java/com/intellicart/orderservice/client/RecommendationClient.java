package com.intellicart.orderservice.client;

import com.intellicart.grpc.ml.RecommendationServiceGrpc;
import com.intellicart.grpc.ml.RecommendationRequest;
import com.intellicart.grpc.ml.RecommendationResponse;
import com.intellicart.grpc.ml.ProductDetail;
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
            // Log the error for observability
            // In a production scenario, we might want to distinguish between timeouts and other errors
            throw new RuntimeException("Failed to fetch recommendations for user " + userId, e);
        }
    }
}
