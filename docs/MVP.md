# MVP Plan

## Version 0.1

The first version should prove the complete RAG flow with citations.

### Scope

- Single user, no login yet.
- PDF upload only at first.
- Extract text page by page.
- Chunk text into overlapping chunks.
- Generate 384-dimensional embeddings using `all-MiniLM-L6-v2`.
- Store chunks in PostgreSQL with pgvector.
- Search by semantic similarity.
- Generate an answer using retrieved chunks.
- Return source citations with document name and page number.

### API Sketch

```text
POST /api/documents/upload
GET  /api/documents
POST /api/search
POST /api/chat
```

### Later Enterprise Features

- JWT authentication
- Organizations/workspaces
- Role-based access control
- Permission-aware retrieval
- DOCX/TXT support
- OCR
- Hybrid search
- Reranking
- Conversation history
- Feedback and evaluation
- Admin dashboard
```
