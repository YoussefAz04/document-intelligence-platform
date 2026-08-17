package com.docintel.backend.document;

public interface RagStatsProjection {
    long getTotalAnswers();
    long getRatedAnswers();
    long getHelpfulAnswers();
    double getAverageLatencyMs();
    double getAverageRetrievedSources();
}
