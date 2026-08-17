# AI-Powered Document Intelligence Platform

An enterprise-style document intelligence platform for uploading documents, indexing their content, and asking natural-language questions with reliable source citations.

The goal of this project is to go beyond a simple "chat with PDF" demo. It is designed as a full-stack RAG system with document ingestion, PostgreSQL/pgvector storage, hybrid retrieval, permission-aware access control, and an Angular user interface.

## Overview

Organizations often store important knowledge across PDFs, Word files, reports, manuals, regulations, invoices, and internal documentation. This platform centralizes those documents and allows users to search and ask questions across them.

Example use case:

```text
User question:
What documents does an international student need for the MSc in Computer Science?

Answer:
International MSc applicants must provide a bachelor's degree, transcript,
English certificate, passport, and recognition statement.

Sources:
- Admission Regulations.pdf, page 12
- Admission Regulations.pdf, page 14
```

Source citation is a core feature because it makes the system auditable and useful in real business, academic, and administrative environments.

## Architecture

```text
Documents
PDF / DOCX / TXT
      |
      v
Document Processing
Text extraction, OCR, metadata detection
      |
      v
Chunking + Embeddings
Page-aware chunks and semantic vectors
      |
      v
PostgreSQL + pgvector
Document metadata, chunks, embeddings, permissions
      |
      v
Hybrid Retrieval
Keyword search + vector search + reranking
      |
      v
RAG Engine
LLM answer generation with citations
      |
      v
Angular Interface
Upload, search, chat, citations, admin dashboard
```

## Tech Stack

| Layer | Technology |
| --- | --- |
| Frontend | Angular |
| Backend API | Spring Boot |
| Security | Spring Security, JWT, RBAC |
| AI Service | Python, FastAPI |
| Embeddings | Sentence Transformers |
| Database | PostgreSQL, pgvector |
| Document Processing | Apache Tika, PyMuPDF |
| Infrastructure | Docker, GitHub Actions |

## Current Status

Implemented:

- Monorepo project structure
- PostgreSQL + pgvector Docker setup
- Initial database schema
- Spring Boot backend
- Backend health endpoint
- Document metadata upload endpoint
- Document listing endpoint
- Basic upload validation for PDF, DOCX, and TXT
- Text extraction with Apache Tika
- Automatic text chunking on upload
- Chunk persistence in PostgreSQL
- FastAPI embedding service
- Sentence Transformer embeddings with `all-MiniLM-L6-v2`
- Embedding persistence in pgvector
- Cosine-similarity semantic search
- PostgreSQL full-text keyword search with ranked results
- Hybrid retrieval using Reciprocal Rank Fusion
- RAG question answering with structured source citations
- Page-aware PDF extraction and citations
- Angular document upload and RAG chat workspace
- IBM Carbon Design System interface with responsive navigation
- Interactive page-aware source detail panel
- Per-answer retrieval and generation latency telemetry
- Explainable RAG confidence levels derived from cited retrieval evidence
- Citation-level semantic, keyword, and hybrid match diagnostics
- Persistent helpful/not-helpful answer feedback
- Aggregate answer-quality and latency statistics
- Flyway database migrations for safe schema evolution

Current API endpoints:

```text
GET  /api/health
GET  /api/documents
POST /api/documents/upload
GET  /api/documents/{documentId}/chunks
GET  /api/search/semantic?query={text}&limit={1-20}
GET  /api/search/keyword?query={text}&limit={1-20}
GET  /api/search/hybrid?query={text}&limit={1-20}
POST /api/rag/ask
PUT  /api/rag/interactions/{interactionId}/feedback
GET  /api/rag/stats
```

## Project Structure

```text
document-intelligence-platform/
  backend/                 Spring Boot API
  ai-service/              FastAPI AI service
  frontend/                Angular application
  docs/                    Architecture and planning notes
  sample-documents/        Local test documents
  infra/postgres/init/     PostgreSQL initialization scripts
  docker-compose.yml       Local infrastructure
```

## Database

The project uses PostgreSQL with the `pgvector` extension.

Start PostgreSQL and the AI service:

```bash
docker compose up -d postgres ai-service
```

The first embedding request downloads the model into a persistent Docker
volume. Open the AI service Swagger UI at <http://localhost:8000/docs>.

For free local answer generation, install Ollama, pull a small chat model, and
run the AI service with the Ollama provider:

```powershell
ollama pull llama3.2
```

Project-root `.env`:

```text
OPENAI_API_KEY=
GENERATION_PROVIDER=ollama
OLLAMA_BASE_URL=http://host.docker.internal:11434
OLLAMA_MODEL=llama3.2
GENERATION_MODEL=llama3.2
```

The `.env` file is ignored by Git and must never be committed. Recreate the AI
container after changing environment variables:

```bash
docker compose up -d --force-recreate ai-service
```

OpenAI can still be used later by setting `GENERATION_PROVIDER=openai`,
`OPENAI_API_KEY`, and an OpenAI generation model. Do not commit real API keys.

Connection details:

```text
Host: localhost
Port: 5433
Database: docintel
Username: docintel
Password: docintel
```

Useful SQL:

```sql
SELECT * FROM documents;
SELECT * FROM document_chunks;
SELECT * FROM rag_interactions ORDER BY created_at DESC;
```

## Backend Setup

Requirements:

- Java 17
- Maven
- Docker Desktop

Run the backend:

```bash
cd backend
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/api/health
```

PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

## Upload A Document

Windows PowerShell 5.1 does not support `Invoke-RestMethod -Form`, so use
`curl.exe` for multipart upload:

```powershell
$response = curl.exe -s -X POST `
  "http://localhost:8080/api/documents/upload" `
  -F "file=@C:\Users\azzou\Downloads\document-intelligence-platform\sample-documents\demo-handbook.txt"

$uploaded = $response | ConvertFrom-Json
$uploaded
```

List uploaded documents:

```powershell
Invoke-RestMethod http://localhost:8080/api/documents
```

Semantic search:

```powershell
Invoke-RestMethod `
  "http://localhost:8080/api/search/semantic?query=documents%20needed%20for%20international%20MSc&limit=5"
```

Keyword search:

```powershell
Invoke-RestMethod `
  "http://localhost:8080/api/search/keyword?query=passport%20transcript&limit=5"
```

Hybrid search combines semantic and keyword rankings with Reciprocal Rank
Fusion. Each result includes the original scores and indicates whether it was
found by `SEMANTIC`, `KEYWORD`, or `BOTH` retrieval methods.

```powershell
Invoke-RestMethod `
  "http://localhost:8080/api/search/hybrid?query=passport%20transcript&limit=5"
```

Ask a grounded RAG question:

```powershell
$body = @{
  question = "What documents does an international MSc student need?"
  limit = 5
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/rag/ask" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

The answer uses inline labels such as `[S1]`. The response also includes an
interaction ID, confidence level, retrieval/generation latency, cited source
counts, and citation-level semantic and keyword evidence.

Submit answer feedback:

```powershell
$feedback = @{ feedback = "HELPFUL" } | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/rag/interactions/$($result.interactionId)/feedback" `
  -Method Put `
  -ContentType "application/json" `
  -Body $feedback
```

Inspect aggregate RAG quality statistics:

```powershell
Invoke-RestMethod http://localhost:8080/api/rag/stats
```

## Frontend Setup

The Angular frontend uses IBM Carbon styles, IBM Plex typography, and Carbon
icons. Its development proxy forwards `/api` requests to Spring Boot on port
8080.

```powershell
cd C:\Users\azzou\Downloads\document-intelligence-platform\frontend
npm install
npm start
```

Open <http://localhost:4200>.

## MVP Roadmap

Next development steps:

1. Add document deletion and re-indexing.
2. Add authentication and organization workspaces.

## Enterprise Features Roadmap

Planned advanced features:

- Multi-user authentication
- Organization workspaces
- Role-based document permissions
- Permission-aware retrieval
- OCR for scanned documents
- Document versioning
- Conversation history
- Admin dashboard
- Offline evaluation datasets and regression scoring
- Retrieval-quality dashboards by workspace and document
- CI/CD pipeline
- Cloud deployment

## Key Engineering Focus

The most important engineering challenge is permission-aware retrieval.

The AI should only retrieve chunks from documents the logged-in user is allowed to access. This prevents the RAG engine from leaking private information through retrieved context.

Example:

```text
ADMIN
  -> HR documents
  -> Finance documents
  -> Technical documents

EMPLOYEE
  -> Public company documents
  -> Technical documentation

HR
  -> HR documents
  -> Employee policies
```

## Portfolio Goal

This project demonstrates:

- Full-stack development with Angular and Spring Boot
- REST API design
- PostgreSQL data modeling
- Vector search with pgvector
- Retrieval-Augmented Generation
- RAG observability, confidence scoring, and feedback analytics
- Document processing pipelines
- Authentication and authorization design
- Docker-based local infrastructure
- Enterprise AI system architecture

## CV Description

Enterprise AI Knowledge Platform - RAG and Semantic Search

Developed a multi-service document intelligence platform supporting document ingestion, semantic search, hybrid retrieval, and RAG-based question answering with page-aware citations. Added retrieval diagnostics, confidence scoring, latency telemetry, persistent user feedback, and quality analytics using Spring Boot, Angular, FastAPI, PostgreSQL, pgvector, Flyway, and Docker.
