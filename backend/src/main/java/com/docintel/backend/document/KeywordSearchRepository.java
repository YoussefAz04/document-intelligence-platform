package com.docintel.backend.document;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KeywordSearchRepository {

    private static final String KEYWORD_SEARCH_SQL = """
            WITH search_query AS (
                SELECT websearch_to_tsquery('english', ?) AS query
            )
            SELECT dc.id AS chunk_id,
                   dc.document_id,
                   d.original_filename,
                   dc.chunk_index,
                   dc.page_number,
                   dc.content,
                   ts_rank_cd(dc.search_vector, search_query.query, 32) AS score
            FROM document_chunks dc
            JOIN documents d ON d.id = dc.document_id
            CROSS JOIN search_query
            WHERE dc.search_vector @@ search_query.query
            ORDER BY score DESC, dc.created_at DESC
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public KeywordSearchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<KeywordSearchResult> search(String query, int limit) {
        return jdbcTemplate.query(
                KEYWORD_SEARCH_SQL,
                (resultSet, rowNumber) -> new KeywordSearchResult(
                        resultSet.getObject("chunk_id", java.util.UUID.class),
                        resultSet.getObject("document_id", java.util.UUID.class),
                        resultSet.getString("original_filename"),
                        resultSet.getInt("chunk_index"),
                        resultSet.getObject("page_number", Integer.class),
                        resultSet.getString("content"),
                        resultSet.getDouble("score")
                ),
                query,
                limit
        );
    }
}
