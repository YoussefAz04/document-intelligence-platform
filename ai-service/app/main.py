from fastapi import Depends, FastAPI

from app.config import EMBEDDING_DIMENSION, EMBEDDING_MODEL
from app.embedding_service import EmbeddingService, get_embedding_service
from app.schemas import EmbeddingRequest, EmbeddingResponse, HealthResponse


app = FastAPI(
    title="Document Intelligence AI Service",
    description="Embedding API for semantic document retrieval.",
    version="0.1.0",
)


@app.get("/api/health", response_model=HealthResponse)
def health(
    service: EmbeddingService = Depends(get_embedding_service),
) -> HealthResponse:
    return HealthResponse(
        status="UP",
        service="document-intelligence-ai-service",
        model=EMBEDDING_MODEL,
        model_loaded=service.model_loaded,
    )


@app.post("/api/embeddings", response_model=EmbeddingResponse)
def create_embeddings(
    request: EmbeddingRequest,
    service: EmbeddingService = Depends(get_embedding_service),
) -> EmbeddingResponse:
    embeddings = service.embed(request.texts)
    return EmbeddingResponse(
        model=EMBEDDING_MODEL,
        dimension=EMBEDDING_DIMENSION,
        embeddings=embeddings,
    )
