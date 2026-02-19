from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import grpc
import structlog
import asyncio
from app.grpc_server import RecommendationService
from app.infrastructure.grpc import ml_pb2_grpc

def inject_ote_context(logger, method_name, event_dict):
    """Injects OpenTelemetry context into the logger."""
    span = trace.get_current_span()
    if span and span.get_span_context().is_valid:
        event_dict["trace_id"] = format(span.get_span_context().trace_id, "032x")
        event_dict["span_id"] = format(span.get_span_context().span_id, "016x")
        event_dict["trace_flags"] = format(span.get_span_context().trace_flags, "01x")
    return event_dict

structlog.configure(
    processors=[
        structlog.contextvars.merge_contextvars,
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso"),
        inject_ote_context, # Correlate with Java
        structlog.processors.StackInfoRenderer(),
        structlog.processors.format_exc_info,
        structlog.processors.JSONRenderer() # Format as JSON
    ],
    logger_factory=structlog.PrinterLoggerFactory(),
    cache_logger_on_first_use=True,
)

logger = structlog.get_logger()

# Global gRPC server
grpc_server = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: Start gRPC server
    global grpc_server
    logger.info("service_startup", action="starting_grpc", port=50051)

    grpc_server = grpc.aio.server()
    ml_pb2_grpc.add_RecommendationServiceServicer_to_server(
        RecommendationService(), grpc_server
    )
    grpc_server.add_insecure_port('[::]:50051')
    await grpc_server.start()

    yield
    logger.info("service_startup", action="ready", grpc_port=50051)
    
    # Shutdown: Stop gRPC server
    if grpc_server:
        logger.info("service_shutdown", action="stopping_grpc")
        await grpc_server.stop(5)

# Initialize FastAPI app with lifespan
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

# Include API routes
from app.api import routes

app.include_router(routes.router, prefix="/api/v1")

@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "ml-service"}
