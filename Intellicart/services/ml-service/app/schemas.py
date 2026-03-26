from pydantic import BaseModel, ConfigDict
from typing import List, Optional

class ProductDetail(BaseModel):
    product_id: str
    score: float
    reason: Optional[str] = None

class RecommendationRequest(BaseModel):
    user_id: str
    max_results: int = 10
    interested_categories: List[str] = []

class RecommendationResponse(BaseModel):
    model_config = ConfigDict(protected_namespaces=())
    products: List[ProductDetail]
    model_version: str
