import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CarbonIconComponent } from './carbon-icon.component';
import { DocumentApiService } from './document-api.service';
import { ChatMessage, DocumentRecord, Notice, RagCitation, RagFeedback, RagStats } from './models';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule, CarbonIconComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  @ViewChild('fileInput') private fileInput?: ElementRef<HTMLInputElement>;

  private readonly api = inject(DocumentApiService);

  readonly activeView = signal<'chat' | 'documents'>('chat');
  readonly navigationOpen = signal(false);
  readonly documents = signal<DocumentRecord[]>([]);
  readonly loadingDocuments = signal(true);
  readonly uploading = signal(false);
  readonly asking = signal(false);
  readonly backendOnline = signal(false);
  readonly selectedCitation = signal<RagCitation | null>(null);
  readonly notice = signal<Notice | null>(null);
  readonly currentModel = signal('llama3.2');
  readonly ragStats = signal<RagStats>({
    totalAnswers: 0,
    ratedAnswers: 0,
    helpfulAnswers: 0,
    feedbackRate: 0,
    helpfulRate: 0,
    averageLatencyMs: 0,
    averageRetrievedSources: 0,
  });
  readonly messages = signal<ChatMessage[]>([this.createWelcomeMessage()]);
  readonly suggestedQuestions = [
    'What documents are required for an international MSc application?',
    'What are the scholarship requirements and deadlines?',
    'Summarize the submission procedure.',
  ];

  readonly indexedDocumentCount = computed(() => this.documents().filter((document) => document.chunkCount > 0).length);
  readonly indexedChunkCount = computed(() => this.documents().reduce((total, document) => total + document.chunkCount, 0));
  readonly isWelcomeState = computed(() => this.messages().length === 1 && !this.asking());
  readonly helpfulRateLabel = computed(() => this.ragStats().ratedAnswers ? `${Math.round(this.ragStats().helpfulRate)}%` : '--');

  question = '';

  ngOnInit(): void {
    this.loadDocuments();
    this.loadRagStats();
    this.api.health().subscribe({ next: () => this.backendOnline.set(true), error: () => this.backendOnline.set(false) });
  }

  setView(view: 'chat' | 'documents'): void { this.activeView.set(view); this.navigationOpen.set(false); this.notice.set(null); }
  toggleNavigation(): void { this.navigationOpen.update((open) => !open); }
  openFilePicker(): void { this.fileInput?.nativeElement.click(); }
  dismissNotice(): void { this.notice.set(null); }
  selectCitation(citation: RagCitation): void { this.selectedCitation.set(citation); }
  useSuggestion(suggestion: string): void { this.question = suggestion; }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.uploadFile(file);
    input.value = '';
  }

  onDragOver(event: DragEvent): void { event.preventDefault(); }
  onDrop(event: DragEvent): void { event.preventDefault(); const file = event.dataTransfer?.files[0]; if (file) this.uploadFile(file); }

  loadDocuments(): void {
    this.loadingDocuments.set(true);
    this.api.listDocuments().subscribe({
      next: (documents) => { this.documents.set(documents); this.loadingDocuments.set(false); this.backendOnline.set(true); },
      error: () => { this.loadingDocuments.set(false); this.backendOnline.set(false); this.showError('Documents unavailable', 'Start the Spring Boot backend and try again.'); },
    });
  }

  loadRagStats(): void {
    this.api.ragStats().subscribe({ next: (stats) => this.ragStats.set(stats) });
  }

  uploadFile(file: File): void {
    if (this.uploading()) return;
    this.uploading.set(true);
    this.notice.set(null);
    this.api.uploadDocument(file).subscribe({
      next: (document) => {
        this.uploading.set(false);
        this.documents.update((documents) => [document, ...documents]);
        this.notice.set({ type: 'success', title: 'Document processed', message: `${document.originalFilename} was indexed into ${document.chunkCount} chunks.` });
      },
      error: (error) => { this.uploading.set(false); this.showError('Upload failed', this.api.errorMessage(error)); },
    });
  }

  askQuestion(): void {
    const cleanQuestion = this.question.trim();
    if (!cleanQuestion || this.asking()) return;
    this.messages.update((messages) => [
      ...(messages.length === 1 ? [] : messages),
      { id: crypto.randomUUID(), role: 'user', content: cleanQuestion },
    ]);
    this.question = '';
    this.asking.set(true);
    this.notice.set(null);
    this.api.ask(cleanQuestion).subscribe({
      next: (response) => {
        this.messages.update((messages) => [...messages, {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: response.answer,
          model: response.model ?? undefined,
          citations: response.citations,
          interactionId: response.interactionId,
          telemetry: response.telemetry,
        }]);
        this.currentModel.set(response.model ?? 'Retrieval only');
        this.selectedCitation.set(response.citations[0] ?? null);
        this.asking.set(false);
        this.loadRagStats();
      },
      error: (error) => { this.asking.set(false); this.showError('Answer unavailable', this.api.errorMessage(error)); },
    });
  }

  clearConversation(): void { this.messages.set([this.createWelcomeMessage()]); this.selectedCitation.set(null); }

  rateAnswer(message: ChatMessage, feedback: RagFeedback): void {
    if (!message.interactionId || message.feedbackSaving || message.feedback === feedback) return;
    this.updateMessage(message.id, { feedbackSaving: true });
    this.api.submitFeedback(message.interactionId, feedback).subscribe({
      next: () => {
        this.updateMessage(message.id, { feedback, feedbackSaving: false });
        this.loadRagStats();
      },
      error: (error) => {
        this.updateMessage(message.id, { feedbackSaving: false });
        this.showError('Feedback not saved', this.api.errorMessage(error));
      },
    });
  }

  formatLatency(milliseconds: number): string {
    return milliseconds >= 1000 ? `${(milliseconds / 1000).toFixed(1)} s` : `${milliseconds} ms`;
  }

  confidenceLabel(confidence: string): string {
    return `${confidence.charAt(0)}${confidence.slice(1).toLowerCase()} confidence`;
  }

  semanticPercent(score: number): number {
    return Math.round(Math.max(0, Math.min(1, score)) * 100);
  }

  answerLines(content: string): Array<{ kind: 'text' | 'bullet'; text: string }> {
    return content
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter(Boolean)
      .map((line) => {
        const bullet = line.match(/^(?:[-*•]|\d+[.)])\s+(.*)$/);
        return bullet
          ? { kind: 'bullet' as const, text: bullet[1] }
          : { kind: 'text' as const, text: line };
      });
  }

  fileType(contentType: string): string {
    if (contentType === 'application/pdf') return 'PDF';
    if (contentType.includes('wordprocessingml')) return 'DOCX';
    return 'TXT';
  }

  private showError(title: string, message: string): void { this.notice.set({ type: 'error', title, message }); }
  private updateMessage(messageId: string, changes: Partial<ChatMessage>): void {
    this.messages.update((messages) => messages.map((message) => message.id === messageId ? { ...message, ...changes } : message));
  }
  private createWelcomeMessage(): ChatMessage { return { id: crypto.randomUUID(), role: 'assistant', content: 'What would you like to know about your indexed documents?' }; }
}
