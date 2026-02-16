import grpc
import structlog
from app.infrastructure.grpc import ml_pb2
from app.infrastructure.grpc import ml_pb2_grpc
from app.core.model import model_instance

logger = structlog.get_logger()

class RecommendationService(ml_pb2_grpc.RecommendationServiceServicer):
    async def GetPersonalizedProducts(
        self, request: ml_pb2.RecommendationRequest, context: grpc.aio.ServicerContext
    ) -> ml_pb2.RecommendationResponse:
        logger.info("Received gRPC request", user_id=request.user_id, max_results=request.max_results)
        
        try:
            # Convert repeated field to list
            categories = list(request.interested_categories)
            
            # Get recommendations from the core model
            recommendations_data = model_instance.predict(
                user_id=request.user_id,
                max_results=request.max_results,
                interested_categories=categories
            )
            
            # Convert to protobuf response
            response_products = []
            for item in recommendations_data:
                product_detail = ml_pb2.ProductDetail(
                    product_id=item["product_id"],
                    score=item["score"],
                    reason=item["reason"]
                )
                response_products.append(product_detail)
            
            return ml_pb2.RecommendationResponse(
                products=response_products,
                model_version=model_instance.version
            )
            
        except Exception as e:
            logger.error("Error generating recommendations", error=str(e))
            await context.abort(grpc.StatusCode.INTERNAL, f"Internal error: {str(e)}")
