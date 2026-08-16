# AI Service

FastAPI service that turns text into normalized 384-dimensional vectors. The
Spring Boot backend will store these vectors in PostgreSQL with pgvector and use
them for semantic retrieval.

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/health` | Service and model status |
| `POST` | `/api/embeddings` | Generate embeddings for up to 64 texts |
| `GET` | `/docs` | Interactive Swagger UI |

The model is loaded lazily on the first embedding request. That first request
can take a few minutes because Docker must download the model once. The model
cache is stored in the `huggingface_cache` Docker volume, so later starts are
much faster.

## Run with Docker (recommended)

From the project root:

```powershell
docker compose up -d --build ai-service
docker compose logs -f ai-service
```

Stop following the logs with `Ctrl+C`; this does not stop the container.

Open Swagger UI at <http://localhost:8000/docs>, or test from PowerShell:

```powershell
Invoke-RestMethod "http://localhost:8000/api/health"

$body = @{
  texts = @(
    "A transcript and passport are required.",
    "The academic calendar begins in September."
  )
} | ConvertTo-Json

$result = Invoke-RestMethod `
  -Uri "http://localhost:8000/api/embeddings" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body

$result.dimension
$result.embeddings.Count
```

Expected values are `384` and `2`.

## Run locally

```powershell
cd ai-service
py -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

## Run tests

```powershell
pip install -r requirements-dev.txt
pytest
```

The tests replace the model with a small fake, so they do not download model
weights and run quickly.
