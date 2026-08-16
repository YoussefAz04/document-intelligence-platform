from functools import lru_cache
from threading import Lock

from sentence_transformers import SentenceTransformer

from app.config import EMBEDDING_DIMENSION, EMBEDDING_MODEL


class EmbeddingService:
    def __init__(self) -> None:
        self._model: SentenceTransformer | None = None
        self._model_lock = Lock()

    @property
    def model_loaded(self) -> bool:
        return self._model is not None

    def embed(self, texts: list[str]) -> list[list[float]]:
        model = self._get_model()
        vectors = model.encode(
            texts,
            batch_size=32,
            show_progress_bar=False,
            convert_to_numpy=True,
            normalize_embeddings=True,
        )

        if vectors.ndim != 2 or vectors.shape[1] != EMBEDDING_DIMENSION:
            raise RuntimeError(
                f"Model returned dimension {vectors.shape[1]}, "
                f"expected {EMBEDDING_DIMENSION}."
            )

        return vectors.astype(float).tolist()

    def _get_model(self) -> SentenceTransformer:
        if self._model is None:
            with self._model_lock:
                if self._model is None:
                    self._model = SentenceTransformer(EMBEDDING_MODEL)
        return self._model


@lru_cache
def get_embedding_service() -> EmbeddingService:
    return EmbeddingService()
