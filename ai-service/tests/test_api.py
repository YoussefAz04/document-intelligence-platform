from fastapi.testclient import TestClient

from app.embedding_service import get_embedding_service
from app.main import app


class FakeEmbeddingService:
    model_loaded = True

    def embed(self, texts: list[str]) -> list[list[float]]:
        return [[0.0] * 384 for _ in texts]


app.dependency_overrides[get_embedding_service] = lambda: FakeEmbeddingService()
client = TestClient(app)


def test_health() -> None:
    response = client.get("/api/health")

    assert response.status_code == 200
    assert response.json()["status"] == "UP"
    assert response.json()["model_loaded"] is True


def test_create_embeddings() -> None:
    response = client.post(
        "/api/embeddings",
        json={"texts": ["first document", "second document"]},
    )

    assert response.status_code == 200
    assert response.json()["dimension"] == 384
    assert len(response.json()["embeddings"]) == 2
    assert len(response.json()["embeddings"][0]) == 384


def test_rejects_blank_text() -> None:
    response = client.post("/api/embeddings", json={"texts": ["   "]})

    assert response.status_code == 422
