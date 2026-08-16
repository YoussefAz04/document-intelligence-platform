package com.docintel.backend.document;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TextChunker {

    private static final int MAX_CHUNK_LENGTH = 1_200;
    private static final int OVERLAP_LENGTH = 200;

    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalizedText = text.trim();
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < normalizedText.length()) {
            int end = Math.min(start + MAX_CHUNK_LENGTH, normalizedText.length());
            end = moveEndToWordBoundary(normalizedText, start, end);

            String chunk = normalizedText.substring(start, end).trim();
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            if (end == normalizedText.length()) {
                break;
            }

            start = Math.max(0, end - OVERLAP_LENGTH);
            start = moveStartToWordBoundary(normalizedText, start);
        }

        return chunks;
    }

    private int moveEndToWordBoundary(String text, int start, int end) {
        if (end == text.length()) {
            return end;
        }

        int boundary = Math.max(text.lastIndexOf('\n', end), text.lastIndexOf(' ', end));
        if (boundary <= start + MAX_CHUNK_LENGTH / 2) {
            return end;
        }

        return boundary;
    }

    private int moveStartToWordBoundary(String text, int start) {
        while (start < text.length() && Character.isWhitespace(text.charAt(start))) {
            start++;
        }

        return start;
    }
}
