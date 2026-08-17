export interface DocumentRecord {
  id: string;
  originalFilename: string;
  contentType: string;
  status: 'UPLOADED' | 'PROCESSING' | 'PROCESSED' | 'FAILED';
  createdAt: string;
  chunkCount: number;
}

export interface RagCitation {
  sourceId: string;
  chunkId: string;
  documentId: string;
  filename: string;
  chunkIndex: number;
  pageNumber: number | null;
  content: string;
  fusedScore: number;
  semanticScore: number | null;
  keywordScore: number | null;
  matchedBy: 'SEMANTIC' | 'KEYWORD' | 'BOTH';
}

export type RagConfidence = 'HIGH' | 'MEDIUM' | 'LOW';
export type RagFeedback = 'HELPFUL' | 'NOT_HELPFUL';

export interface RagTelemetry {
  confidence: RagConfidence;
  retrievedSourceCount: number;
  citedSourceCount: number;
  retrievalDurationMs: number;
  generationDurationMs: number;
  totalDurationMs: number;
}

export interface RagAnswer {
  interactionId: string;
  question: string;
  answer: string;
  model: string | null;
  citations: RagCitation[];
  telemetry: RagTelemetry;
}

export interface RagFeedbackResponse { interactionId: string; feedback: RagFeedback; comment: string | null; }

export interface RagStats {
  totalAnswers: number;
  ratedAnswers: number;
  helpfulAnswers: number;
  feedbackRate: number;
  helpfulRate: number;
  averageLatencyMs: number;
  averageRetrievedSources: number;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  model?: string;
  citations?: RagCitation[];
  interactionId?: string;
  telemetry?: RagTelemetry;
  feedback?: RagFeedback;
  feedbackSaving?: boolean;
}
export interface Notice { type: 'success' | 'error'; title: string; message: string; }
