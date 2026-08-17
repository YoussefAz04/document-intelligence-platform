# Document Intelligence Frontend

Angular 21 interface for the document intelligence platform, built with the IBM Carbon Design System.

## Included

- Carbon application shell and responsive navigation
- Document upload with PDF, DOCX, and TXT support
- Indexed document inventory and processing status
- RAG chat connected to the Spring Boot API
- Page-aware source citation details
- Loading, success, offline, and API error states

## Run Locally

Start PostgreSQL, the AI service, and the Spring Boot backend first. Then run:

```powershell
cd C:\Users\azzou\Downloads\document-intelligence-platform\frontend
npm start
```

Open <http://localhost:4200>. The Angular development proxy forwards `/api` requests to `http://localhost:8080`.

## Verify

```powershell
npm test -- --watch=false
npm run build
```
