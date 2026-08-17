package com.docintel.backend.document;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RagInteractionRepository extends JpaRepository<RagInteraction, UUID> {

    @Query(value = """
            SELECT
                COUNT(*) AS "totalAnswers",
                COUNT(feedback) AS "ratedAnswers",
                COUNT(*) FILTER (WHERE feedback = 'HELPFUL') AS "helpfulAnswers",
                COALESCE(AVG(total_duration_ms), 0) AS "averageLatencyMs",
                COALESCE(AVG(retrieved_source_count), 0) AS "averageRetrievedSources"
            FROM rag_interactions
            """, nativeQuery = true)
    RagStatsProjection summarize();
}
