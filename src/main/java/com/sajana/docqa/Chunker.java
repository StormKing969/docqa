package com.sajana.docqa;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Chunker {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        String cleaned = text.replaceAll("\\s+", " ").trim();

        int start = 0;
        while (start < cleaned.length()) {
            int end = Math.min(start + CHUNK_SIZE, cleaned.length());

            // Try to break on a sentence boundary if we're not at the end
            if (end < cleaned.length()) {
                int lastPeriod = cleaned.lastIndexOf(". ", end);
                if (lastPeriod > start + CHUNK_SIZE / 2) {
                    end = lastPeriod + 1;
                }
            }

            chunks.add(cleaned.substring(start, end).trim());

            if (end >= cleaned.length()) break;
            start = end - CHUNK_OVERLAP;
        }

        return chunks;
    }
}