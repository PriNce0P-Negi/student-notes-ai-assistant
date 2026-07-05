package com.prince.notesai.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {

    private static final int CHUNK_SIZE = 900;
    private static final int OVERLAP = 150;
    private static final int MIN_CHUNK_SIZE = 50;
    private static final int BOUNDARY_LOOK_BACK = 200;

    public List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        text = text.replaceAll("\\r\\n", "\n")
                   .replaceAll("[ \\t]+\\n", "\n")
                   .replaceAll("\\n{3,}", "\n\n")
                   .strip();

        int start = 0;
        int textLength = text.length();

        while (start < textLength) {
            int end = Math.min(start + CHUNK_SIZE, textLength);

            if (end < textLength) {
                int boundary = findSentenceBoundary(text, end);
                if (boundary > start + MIN_CHUNK_SIZE) {
                    end = boundary;
                }
            }

            String chunk = text.substring(start, end).strip();

            if (chunk.length() >= MIN_CHUNK_SIZE) {
                chunks.add(chunk);
            }

            int nextStart = end - OVERLAP;

            if (nextStart <= start) {
                nextStart = end;
            }

            start = nextStart;
        }

        return chunks;
    }

    private int findSentenceBoundary(String text, int position) {
        int lookBackLimit = Math.max(position - BOUNDARY_LOOK_BACK, 0);

        for (int i = position; i > lookBackLimit; i--) {
            char c = text.charAt(i);
            if (c == '.' || c == '!' || c == '?' || c == '\n') {
                return Math.min(i + 1, text.length());
            }
        }

        return position;
    }
}