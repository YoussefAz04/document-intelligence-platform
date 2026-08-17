package com.docintel.backend.document;

public record RagStatsResponse(
        long totalAnswers,
        long ratedAnswers,
        long helpfulAnswers,
        double feedbackRate,
        double helpfulRate,
        long averageLatencyMs,
        double averageRetrievedSources
) {
    public static RagStatsResponse from(RagStatsProjection stats) {
        double feedbackRate = stats.getTotalAnswers() == 0
                ? 0
                : percentage(stats.getRatedAnswers(), stats.getTotalAnswers());
        double helpfulRate = stats.getRatedAnswers() == 0
                ? 0
                : percentage(stats.getHelpfulAnswers(), stats.getRatedAnswers());

        return new RagStatsResponse(
                stats.getTotalAnswers(),
                stats.getRatedAnswers(),
                stats.getHelpfulAnswers(),
                feedbackRate,
                helpfulRate,
                Math.round(stats.getAverageLatencyMs()),
                stats.getAverageRetrievedSources()
        );
    }

    private static double percentage(long value, long total) {
        return Math.round((value * 10_000.0) / total) / 100.0;
    }
}
