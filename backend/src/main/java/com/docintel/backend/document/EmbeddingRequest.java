package com.docintel.backend.document;

import java.util.List;

public record EmbeddingRequest(List<String> texts) {
}
