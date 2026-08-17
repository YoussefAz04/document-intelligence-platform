CREATE TABLE IF NOT EXISTS rag_interactions (
    id UUID PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    model VARCHAR(120),
    confidence VARCHAR(20) NOT NULL,
    retrieved_source_count INTEGER NOT NULL,
    cited_source_count INTEGER NOT NULL,
    retrieval_duration_ms BIGINT NOT NULL,
    generation_duration_ms BIGINT NOT NULL,
    total_duration_ms BIGINT NOT NULL,
    feedback VARCHAR(20),
    feedback_comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_rag_interactions_created_at ON rag_interactions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_rag_interactions_feedback ON rag_interactions(feedback);
