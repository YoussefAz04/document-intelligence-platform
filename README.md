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

## Planned Architecture

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

Current API endpoints:

```text
GET  /api/health
GET  /api/documents
POST /api/documents/upload
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

Start the database:

```bash
docker compose up -d postgres
```

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

PowerShell example:

```powershell
$form = @{
  file = Get-Item "C:\Users\azzou\Downloads\document-intelligence-platform\sample-documents\demo-handbook.txt"
}

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/documents/upload" `
  -Method Post `
  -Form $form
```

List uploaded documents:

```powershell
Invoke-RestMethod http://localhost:8080/api/documents
```

## MVP Roadmap

Next development steps:

1. Extract text from uploaded PDF, DOCX, and TXT files.
2. Split extracted text into page-aware chunks.
3. Store chunks in `document_chunks`.
4. Generate embeddings with a Python FastAPI service.
5. Store embeddings in PostgreSQL using pgvector.
6. Implement semantic search.
7. Add keyword search with PostgreSQL full-text search.
8. Combine both into hybrid retrieval.
9. Generate RAG answers with source citations.
10. Build the Angular upload and chat interface.

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
- Usage statistics
- Feedback collection
- RAG evaluation
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
- Document processing pipelines
- Authentication and authorization design
- Docker-based local infrastructure
- Enterprise AI system architecture

## CV Description

Enterprise AI Knowledge Platform - RAG and Semantic Search

Developed a multi-service document intelligence platform supporting document ingestion, semantic search, hybrid retrieval, and RAG-based question answering with source citations. Built with Spring Boot, Angular, FastAPI, PostgreSQL, pgvector, and Docker, with a focus on permission-aware retrieval for enterprise document access.
