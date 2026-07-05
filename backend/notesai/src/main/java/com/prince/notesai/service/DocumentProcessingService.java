package com.prince.notesai.service;

import com.prince.notesai.entity.Document;
import com.prince.notesai.entity.DocumentChunk;
import com.prince.notesai.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private final TextChunkingService textChunkingService;
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final DocumentChunkRepository chunkRepository;

    public void process(Document document, Map<Integer, String> pageTexts) {

        log.info("═══════════════════════════════════════════════════");
        log.info("[Processing] Starting: '{}' (id={}, session={})",
                document.getOriginalFileName(),
                document.getId(),
                document.getSessionId());
        log.info("[Processing] Pages with text: {}", pageTexts.size());
        log.info("═══════════════════════════════════════════════════");

        AtomicInteger totalChunks = new AtomicInteger(0);
        int chunkGlobalIndex = 0;

        for (Map.Entry<Integer, String> entry : pageTexts.entrySet()) {

            int pageNumber = entry.getKey();
            String pageText = entry.getValue();

            List<String> chunks = textChunkingService.splitIntoChunks(pageText);

            log.debug("[Processing] Page {} → {} chunk(s)", pageNumber, chunks.size());

            for (int i = 0; i < chunks.size(); i++) {

                String chunkText = chunks.get(i);

                DocumentChunk chunk = DocumentChunk.builder()
                        .document(document)
                        .chunkIndex(chunkGlobalIndex)
                        .pageNumber(pageNumber)
                        .content(chunkText)
                        .build();

                chunk = chunkRepository.save(chunk);

                List<Float> embedding = embeddingService.generateEmbedding(chunkText);

                qdrantService.uploadVector(
                        chunk.getId(),
                        document.getSessionId(),
                        document.getId(),
                        chunkGlobalIndex,
                        pageNumber,
                        embedding
                );

                chunk.setVectorId(String.valueOf(chunk.getId()));
                chunkRepository.save(chunk);

                log.info("[Processing] ✓ Chunk {}: page={}, idx={}, dim={}",
                        totalChunks.incrementAndGet(),
                        pageNumber,
                        chunkGlobalIndex,
                        embedding.size()
                );

                chunkGlobalIndex++;
            }
        }

        log.info("═══════════════════════════════════════════════════");
        log.info("[Processing] ✅ Complete: '{}' — {} total chunks embedded.",
                document.getOriginalFileName(), totalChunks.get());
        log.info("═══════════════════════════════════════════════════");
    }

    public void process(Document document, String pdfText) {
        log.warn("[Processing] Using legacy flat-text processing for '{}' — page numbers will all be 1.",
                document.getOriginalFileName());
        process(document, Map.of(1, pdfText));
    }
}