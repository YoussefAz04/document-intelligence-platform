import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DocumentRecord, RagAnswer, RagFeedback, RagFeedbackResponse, RagStats } from './models';

@Injectable({ providedIn: 'root' })
export class DocumentApiService {
  private readonly http = inject(HttpClient);
  health(): Observable<unknown> { return this.http.get('/api/health'); }
  listDocuments(): Observable<DocumentRecord[]> { return this.http.get<DocumentRecord[]>('/api/documents'); }
  uploadDocument(file: File): Observable<DocumentRecord> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<DocumentRecord>('/api/documents/upload', formData);
  }
  ask(question: string): Observable<RagAnswer> { return this.http.post<RagAnswer>('/api/rag/ask', { question, limit: 5 }); }
  ragStats(): Observable<RagStats> { return this.http.get<RagStats>('/api/rag/stats'); }
  submitFeedback(interactionId: string, feedback: RagFeedback): Observable<RagFeedbackResponse> {
    return this.http.put<RagFeedbackResponse>(`/api/rag/interactions/${interactionId}/feedback`, { feedback });
  }
  errorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const apiMessage = error.error?.message;
      if (typeof apiMessage === 'string' && apiMessage.trim()) return apiMessage;
      if (error.status === 0) return 'The backend could not be reached.';
    }
    return 'An unexpected error occurred.';
  }
}
