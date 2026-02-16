from fastapi import APIRouter, HTTPException, Depends
from typing import List
from app.schemas import RecommendationRequest, RecommendationResponse, ProductDetail
from app.core.model import model_instance
import structlog

router = APIRouter()
logger = structlog.get_logger()

@router.post("/recommendations", response_model=RecommendationResponse)
async def get_recommendations(request: RecommendationRequest):
    logger.info("REST recommendation request", user_id=request.user_id)
    
    try:
        recommendations_data = model_instance.predict(
            user_id=request.user_id,
            max_results=request.max_results,
            interested_categories=request.interested_categories
        )
        
        response_products = []
        for item in recommendations_data:
            response_products.append(ProductDetail(**item))
            
        return RecommendationResponse(
            products=response_products,
            model_version=model_instance.version
        )
        
    except Exception as e:
        logger.error("Error processing REST request", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))
