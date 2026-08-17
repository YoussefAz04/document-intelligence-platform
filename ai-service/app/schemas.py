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
    generation_provider: str
    generation_model: str
    generation_configured: bool


class ContextSource(BaseModel):
    sourceId: str = Field(pattern=r"^S[1-9][0-9]*$")
    filename: str = Field(min_length=1, max_length=500)
    pageNumber: int | None = Field(default=None, ge=1)
    content: str = Field(min_length=1, max_length=10_000)


class GenerationRequest(BaseModel):
    question: str = Field(min_length=1, max_length=2_000)
    sources: list[ContextSource] = Field(min_length=1, max_length=10)

    @field_validator("question")
    @classmethod
    def clean_question(cls, question: str) -> str:
        question = question.strip()
        if not question:
            raise ValueError("Question must not be blank.")
        return question


class GenerationResponse(BaseModel):
    model: str
    answer: str
