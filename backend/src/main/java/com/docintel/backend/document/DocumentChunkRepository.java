package com.docintel.backend.document;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    long countByDocumentId(UUID documentId);

    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(UUID documentId);
}
