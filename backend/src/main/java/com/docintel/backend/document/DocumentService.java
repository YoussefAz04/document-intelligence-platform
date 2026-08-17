package com.docintel.backend.document;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "txt");

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentChunkEmbeddingRepository documentChunkEmbeddingRepository;
    private final EmbeddingClient embeddingClient;
    private final TextExtractionService textExtractionService;
    private final TextChunker textChunker;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            DocumentChunkEmbeddingRepository documentChunkEmbeddingRepository,
            EmbeddingClient embeddingClient,
            TextExtractionService textExtractionService,
            TextChunker textChunker
    ) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.documentChunkEmbeddingRepository = documentChunkEmbeddingRepository;
        this.embeddingClient = embeddingClient;
        this.textExtractionService = textExtractionService;
        this.textChunker = textChunker;
    }

    @Transactional
    public DocumentResponse upload(MultipartFile file) {
        validateFile(file);

        String filename = cleanFilename(file.getOriginalFilename());
        String contentType = resolveContentType(file, filename);
        Document document = new Document(filename, contentType);
        document.markProcessing();
        documentRepository.save(document);

        List<PendingChunk> pendingChunks = textExtractionService.extract(file, contentType)
                .stream()
                .flatMap(page -> textChunker.chunk(page.content())
                        .stream()
                        .map(content -> new PendingChunk(page.pageNumber(), content)))
                .toList();
        if (pendingChunks.isEmpty()) {
            document.markFailed();
            throw new IllegalArgumentException("No readable text could be extracted from the uploaded document.");
        }

        List<DocumentChunk> chunks = new ArrayList<>(pendingChunks.size());
        for (int index = 0; index < pendingChunks.size(); index++) {
            PendingChunk pendingChunk = pendingChunks.get(index);
            chunks.add(new DocumentChunk(document, index, pendingChunk.pageNumber(), pendingChunk.content()));
        }

        documentChunkRepository.saveAllAndFlush(chunks);
        List<String> chunkContents = pendingChunks.stream()
                .map(PendingChunk::content)
                .toList();
        List<List<Double>> embeddings = embeddingClient.createEmbeddings(chunkContents);
        documentChunkEmbeddingRepository.updateEmbeddings(chunks, embeddings);
        document.markProcessed();
        documentRepository.saveAndFlush(document);

        return DocumentResponse.from(document, chunks.size());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findAll() {
        return documentRepository.findAll()
                .stream()
                .map(document -> DocumentResponse.from(document, documentChunkRepository.countByDocumentId(document.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentChunkResponse> findChunks(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new IllegalArgumentException("Document not found.");
        }

        return documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId)
                .stream()
                .map(DocumentChunkResponse::from)
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty.");
        }

        String filename = cleanFilename(file.getOriginalFilename());
        String contentType = file.getContentType();
        if (!hasAllowedContentType(contentType) && !hasAllowedExtension(filename)) {
            throw new IllegalArgumentException("Only PDF, DOCX, and TXT files are supported right now.");
        }
    }

    private boolean hasAllowedContentType(String contentType) {
        return contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT));
    }

    private boolean hasAllowedExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return false;
        }

        String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private String resolveContentType(MultipartFile file, String filename) {
        String contentType = file.getContentType();
        if (hasAllowedContentType(contentType)) {
            return contentType.toLowerCase(Locale.ROOT);
        }

        if (filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return "application/pdf";
        }
        if (filename.toLowerCase(Locale.ROOT).endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if (filename.toLowerCase(Locale.ROOT).endsWith(".txt")) {
            return "text/plain";
        }

        return "application/octet-stream";
    }

    private String cleanFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "uploaded-document";
        }

        String normalizedFilename = originalFilename.replace("\\", "/");
        return normalizedFilename.substring(normalizedFilename.lastIndexOf('/') + 1);
    }

    private record PendingChunk(Integer pageNumber, String content) {
    }
}
