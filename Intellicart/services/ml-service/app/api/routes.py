from fastapi import APIRouter, HTTPException, Query, Path, Body
from typing import List, Optional, Annotated
from app.schemas import RecommendationRequest, RecommendationResponse, ProductDetail
from app.core.model import model_instance
import structlog

router = APIRouter()
logger = structlog.get_logger()


# -------------------------
# POST - Recomendación formal
# -------------------------
@router.post("/recommendations", response_model=RecommendationResponse)
async def get_recommendations_post(
    request: Annotated[RecommendationRequest, Body()]
):
    logger.info(
        "recommendation_request_received",
        method="POST",
        user_id=request.user_id
    )

    try:
        recommendations = model_instance.predict(
            user_id=request.user_id,
            max_results=request.max_results,
            interested_categories=request.interested_categories
        )

        return RecommendationResponse(
            products=[ProductDetail(**item) for item in recommendations],
            model_version=model_instance.version
        )

    except Exception as e:
        logger.error(
            "recommendation_error",
            user_id=request.user_id,
            error=str(e)
        )
        raise HTTPException(status_code=500, detail="Error generating recommendations")


# -------------------------
# GET - Para gateway actual
# -------------------------
# Usamos :path para que acepte puntos y otros caracteres especiales en el email
@router.get("/recommendations/{user_id:path}", response_model=RecommendationResponse)
async def get_recommendations_get(
    user_id: str,
    max_results: int = Query(5, ge=1, le=50)
):
    logger.info(
        "recommendation_request_received",
        method="GET",
        user_id=user_id
    )

    try:
        recommendations = model_instance.predict(
            user_id=user_id,
            max_results=max_results,
            interested_categories=None
        )

        return RecommendationResponse(
            products=[ProductDetail(**item) for item in recommendations],
            model_version=model_instance.version
        )

    except Exception as e:
        logger.error(
            "recommendation_error",
            user_id=user_id,
            error=str(e)
        )
        raise HTTPException(status_code=500, detail="Error generating recommendations")

# Fallback para cuando el user_id viene en query por si acaso el frontend cambia
@router.get("/recommendations", response_model=RecommendationResponse)
async def get_recommendations_query(
    user_id: Optional[str] = Query(None, description="The user ID or email"),
    max_results: int = Query(5, ge=1, le=50)
):
    if not user_id:
         # Si no hay user_id en query ni en path (esta ruta captura /recommendations sin path param)
         raise HTTPException(status_code=422, detail="user_id is required in query or path")
         
    logger.info(
        "recommendation_request_received_query",
        method="GET",
        user_id=user_id
    )
    return await get_recommendations_get(user_id=user_id, max_results=max_results)
