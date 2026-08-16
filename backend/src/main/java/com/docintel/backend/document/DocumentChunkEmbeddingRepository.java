package com.docintel.backend.document;

import java.util.List;
import java.util.StringJoiner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentChunkEmbeddingRepository {

    private static final String UPDATE_EMBEDDING_SQL = """
            UPDATE document_chunks
            SET embedding = CAST(? AS vector)
            WHERE id = ?
            """;

    private static final String SEMANTIC_SEARCH_SQL = """
            SELECT dc.id AS chunk_id,
                   dc.document_id,
                   d.original_filename,
                   dc.chunk_index,
                   dc.page_number,
                   dc.content,
                   1 - (dc.embedding <=> CAST(? AS vector)) AS score
            FROM document_chunks dc
            JOIN documents d ON d.id = dc.document_id
            WHERE dc.embedding IS NOT NULL
            ORDER BY dc.embedding <=> CAST(? AS vector)
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public DocumentChunkEmbeddingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void updateEmbeddings(List<DocumentChunk> chunks, List<List<Double>> embeddings) {
        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException("Every chunk must have exactly one embedding.");
        }

        for (int index = 0; index < chunks.size(); index++) {
            jdbcTemplate.update(
                    UPDATE_EMBEDDING_SQL,
                    toVectorLiteral(embeddings.get(index)),
                    chunks.get(index).getId()
            );
        }
    }

    public List<SemanticSearchResult> search(List<Double> queryEmbedding, int limit) {
        String vector = toVectorLiteral(queryEmbedding);
        return jdbcTemplate.query(
                SEMANTIC_SEARCH_SQL,
                (resultSet, rowNumber) -> new SemanticSearchResult(
                        resultSet.getObject("chunk_id", java.util.UUID.class),
                        resultSet.getObject("document_id", java.util.UUID.class),
                        resultSet.getString("original_filename"),
                        resultSet.getInt("chunk_index"),
                        resultSet.getObject("page_number", Integer.class),
                        resultSet.getString("content"),
                        resultSet.getDouble("score")
                ),
                vector,
                vector,
                limit
        );
    }

    private String toVectorLiteral(List<Double> embedding) {
        StringJoiner values = new StringJoiner(",", "[", "]");
        embedding.forEach(value -> values.add(Double.toString(value)));
        return values.toString();
    }
}
