from fastapi import Depends, FastAPI, HTTPException

from app.config import (
    EMBEDDING_DIMENSION,
    EMBEDDING_MODEL,
    GENERATION_MODEL,
    GENERATION_PROVIDER,
)
from app.embedding_service import EmbeddingService, get_embedding_service
from app.generation_service import (
    GenerationNotConfiguredError,
    GenerationService,
    GenerationServiceError,
    get_generation_service,
)
from app.schemas import (
    EmbeddingRequest,
    EmbeddingResponse,
    GenerationRequest,
    GenerationResponse,
    HealthResponse,
)


app = FastAPI(
    title="Document Intelligence AI Service",
    description="Embedding and grounded answer generation for document retrieval.",
    version="0.2.0",
)


@app.get("/api/health", response_model=HealthResponse)
def health(
    service: EmbeddingService = Depends(get_embedding_service),
    generation_service: GenerationService = Depends(get_generation_service),
) -> HealthResponse:
    return HealthResponse(
        status="UP",
        service="document-intelligence-ai-service",
        model=EMBEDDING_MODEL,
        model_loaded=service.model_loaded,
        generation_provider=GENERATION_PROVIDER,
        generation_model=GENERATION_MODEL,
        generation_configured=generation_service.configured,
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


@app.post("/api/generate", response_model=GenerationResponse)
def generate_answer(
    request: GenerationRequest,
    service: GenerationService = Depends(get_generation_service),
) -> GenerationResponse:
    try:
        answer = service.generate(request.question, request.sources)
    except GenerationNotConfiguredError as exception:
        raise HTTPException(status_code=503, detail=str(exception)) from exception
    except GenerationServiceError as exception:
        raise HTTPException(status_code=502, detail=str(exception)) from exception

    return GenerationResponse(model=GENERATION_MODEL, answer=answer)
