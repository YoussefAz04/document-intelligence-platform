from fastapi.testclient import TestClient

from app.embedding_service import get_embedding_service
from app.generation_service import get_generation_service
from app.main import app


class FakeEmbeddingService:
    model_loaded = True

    def embed(self, texts: list[str]) -> list[list[float]]:
        return [[0.0] * 384 for _ in texts]


class FakeGenerationService:
    configured = True

    def generate(self, question: str, sources: list[object]) -> str:
        return "Applicants need a passport and transcript [S1]."


app.dependency_overrides[get_embedding_service] = lambda: FakeEmbeddingService()
app.dependency_overrides[get_generation_service] = lambda: FakeGenerationService()
client = TestClient(app)


def test_health() -> None:
    response = client.get("/api/health")

    assert response.status_code == 200
    assert response.json()["status"] == "UP"
    assert response.json()["model_loaded"] is True
    assert response.json()["generation_configured"] is True


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


def test_generate_grounded_answer() -> None:
    response = client.post(
        "/api/generate",
        json={
            "question": "What documents are required?",
            "sources": [
                {
                    "sourceId": "S1",
                    "filename": "handbook.txt",
                    "pageNumber": None,
                    "content": "Applicants need a passport and transcript.",
                }
            ],
        },
    )

    assert response.status_code == 200
    assert response.json()["answer"].endswith("[S1].")


def test_rejects_generation_without_sources() -> None:
    response = client.post(
        "/api/generate",
        json={"question": "What documents are required?", "sources": []},
    )

    assert response.status_code == 422
