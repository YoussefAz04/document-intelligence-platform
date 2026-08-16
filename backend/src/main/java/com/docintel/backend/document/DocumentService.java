package com.docintel.backend.document;

import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Transactional
    public DocumentResponse upload(MultipartFile file) {
        validateFile(file);

        String filename = cleanFilename(file.getOriginalFilename());
        String contentType = resolveContentType(file, filename);
        Document document = new Document(filename, contentType);

        return DocumentResponse.from(documentRepository.save(document));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findAll() {
        return documentRepository.findAll()
                .stream()
                .map(DocumentResponse::from)
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
}
