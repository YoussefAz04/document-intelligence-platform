import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { App } from './app';
import { DocumentApiService } from './document-api.service';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        {
          provide: DocumentApiService,
          useValue: {
            listDocuments: () => of([]),
            ragStats: () => of({ totalAnswers: 0, ratedAnswers: 0, helpfulAnswers: 0, feedbackRate: 0, helpfulRate: 0, averageLatencyMs: 0, averageRetrievedSources: 0 }),
            health: () => of({ status: 'UP' }),
          },
        },
      ],
    }).compileComponents();
  });

  it('creates the document intelligence workspace', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect((fixture.nativeElement as HTMLElement).querySelector('h1')?.textContent).toContain(
      'Knowledge workspace',
    );
  });

  it('shows the documents view from the primary navigation', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    fixture.componentInstance.setView('documents');
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('h1')?.textContent).toContain(
      'Documents',
    );
  });
});
