# AI-Powered Document Intelligence Platform

Enterprise-style RAG platform for uploading documents, extracting text, searching with PostgreSQL/pgvector, and answering questions with source citations.

## Target Stack

- Frontend: Angular
- Main backend: Spring Boot, Spring Security, JWT, RBAC
- AI service: Python, FastAPI, Sentence Transformers
- Database: PostgreSQL + pgvector
- Document processing: Apache Tika / PyMuPDF
- Infrastructure: Docker, GitHub Actions later

## First MVP Goal

Build one vertical slice:

1. Upload one PDF.
2. Extract text with page numbers.
3. Split text into chunks.
4. Store chunks in PostgreSQL.
5. Generate embeddings.
6. Search relevant chunks.
7. Ask a question.
8. Return answer with citations.

## Run Database

```bash
docker compose up -d postgres
```

PostgreSQL will run on port `5433`.

Connection details:

```text
Database: docintel
User: docintel
Password: docintel
Host: localhost
Port: 5433
```

## Project Structure

```text
backend/                 Spring Boot API
ai-service/              FastAPI AI service
frontend/                Angular app
docs/                    Architecture notes and planning
sample-documents/        PDFs/DOCX/TXT files for local testing
infra/postgres/init/     PostgreSQL initialization scripts
```

## Build Order

1. Start PostgreSQL + pgvector.
2. Create Spring Boot backend.
3. Add document upload endpoint.
4. Create FastAPI text extraction and embedding service.
5. Store chunks and embeddings.
6. Add semantic search.
7. Add RAG answer endpoint.
8. Build Angular UI.
