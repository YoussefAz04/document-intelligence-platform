from pydantic import BaseModel, Field, field_validator

from app.config import MAX_BATCH_SIZE


class EmbeddingRequest(BaseModel):
    texts: list[str] = Field(min_length=1, max_length=MAX_BATCH_SIZE)

    @field_validator("texts")
    @classmethod
    def validate_texts(cls, texts: list[str]) -> list[str]:
        cleaned = [text.strip() for text in texts]
        if any(not text for text in cleaned):
            raise ValueError("Texts must not be blank.")
        return cleaned


class EmbeddingResponse(BaseModel):
    model: str
    dimension: int
    embeddings: list[list[float]]


class HealthResponse(BaseModel):
    status: str
    service: str
    model: str
    model_loaded: bool
