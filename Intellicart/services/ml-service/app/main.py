from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import grpc
import structlog
import asyncio
from app.grpc_server import RecommendationService
from app.infrastructure.grpc import ml_pb2_grpc

logger = structlog.get_logger()

# Global gRPC server
grpc_server = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: Start gRPC server
    global grpc_server
    logger.info("Starting gRPC Server on port 50051...")
    grpc_server = grpc.aio.server()
    ml_pb2_grpc.add_RecommendationServiceServicer_to_server(RecommendationService(), grpc_server)
    grpc_server.add_insecure_port('[::]:50051')
    await grpc_server.start()
    logger.info("gRPC Server started successfully")
    
    yield
    
    # Shutdown: Stop gRPC server
    if grpc_server:
        logger.info("Stopping gRPC Server...")
        await grpc_server.stop(5)
        logger.info("gRPC Server stopped")

app = FastAPI(
    title="Intellicart ML Service",
    description="Machine Learning Service for Intellicart",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from app.api import routes

app.include_router(routes.router, prefix="/api/v1")

@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "ml-service"}
