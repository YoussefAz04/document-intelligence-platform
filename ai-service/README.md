# AI Service

FastAPI service that creates normalized 384-dimensional vectors and generates
grounded answers from retrieved document excerpts. The Spring Boot backend
stores embeddings in pgvector and owns retrieval and citation metadata.

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/health` | Service and model status |
| `POST` | `/api/embeddings` | Generate embeddings for up to 64 texts |
| `POST` | `/api/generate` | Generate an answer from labeled source excerpts |
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

## Answer generation with Ollama

For a free local setup, install Ollama on Windows and pull a model:

```powershell
ollama pull llama3.2
```

Then use this in the project-root `.env` file:

```text
OPENAI_API_KEY=
GENERATION_PROVIDER=ollama
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=llama3.2
GENERATION_MODEL=llama3.2
```

Recreate the service after changing `.env`:

```powershell
docker compose up -d --force-recreate ai-service
```

Inside Docker, `host.docker.internal` lets FastAPI call the Ollama app running
on Windows. The generation prompt requires inline source labels and instructs
the model to use only the supplied excerpts.

OpenAI can be used later by setting `GENERATION_PROVIDER=openai`,
`OPENAI_API_KEY`, and an OpenAI generation model. API keys must never be
committed.

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
