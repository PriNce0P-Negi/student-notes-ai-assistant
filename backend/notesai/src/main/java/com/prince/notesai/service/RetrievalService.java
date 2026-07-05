package com.prince.notesai.service;

import com.prince.notesai.dto.QdrantSearchResponse;
import com.prince.notesai.dto.RelevantChunk;
import com.prince.notesai.entity.Document;
import com.prince.notesai.entity.DocumentChunk;
import com.prince.notesai.repository.DocumentChunkRepository;
import com.prince.notesai.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetrievalService {

    private final DocumentChunkRepository chunkRepository;
    private final DocumentRepository documentRepository;

    public List<RelevantChunk> getRelevantChunks(QdrantSearchResponse response) {

        List<RelevantChunk> relevantChunks = new ArrayList<>();

        if (response == null || response.getResult() == null) {
            log.warn("[Retrieval] Qdrant returned null or empty response.");
            return relevantChunks;
        }

        for (QdrantSearchResponse.SearchResult result : response.getResult()) {

            if (result.getPayload() == null) {
                log.warn("[Retrieval] Skipping result with null payload (id={})", result.getId());
                continue;
            }

            Long documentId = result.getPayload().getDocumentId();
            Integer chunkIndex = result.getPayload().getChunkIndex();

            Document document = documentRepository.findById(documentId).orElse(null);
            if (document == null) {
                log.warn("[Retrieval] Document not found in DB: id={}", documentId);
                continue;
            }

            DocumentChunk chunk = chunkRepository
                    .findByDocumentIdAndChunkIndex(documentId, chunkIndex)
                    .orElse(null);

            if (chunk == null) {
                log.warn("[Retrieval] Chunk not found in DB: docId={}, chunkIndex={}",
                        documentId, chunkIndex);
                continue;
            }

            relevantChunks.add(
                    RelevantChunk.builder()
                            .documentId(document.getId())
                            .documentName(document.getOriginalFileName())
                            .chunkId(chunk.getId())
                            .chunkIndex(chunk.getChunkIndex())
                            .pageNumber(chunk.getPageNumber())
                            .score(result.getScore() == null
                                    ? null
                                    : result.getScore().doubleValue())
                            .content(chunk.getContent())
                            .build()
            );
        }

        log.info("[Retrieval] Mapped {} relevant chunks from {} Qdrant results.",
                relevantChunks.size(), response.getResult().size());

        return relevantChunks;
    }

    public List<RelevantChunk> getRelevantChunks(
            QdrantSearchResponse response,
            String sessionId
    ) {
        List<RelevantChunk> all = getRelevantChunks(response);

        if (sessionId == null || sessionId.isBlank()) {
            return all;
        }

        return all.stream()
                .filter(chunk -> {
                    Document doc = documentRepository
                            .findById(chunk.getDocumentId())
                            .orElse(null);
                    return doc != null && sessionId.equals(doc.getSessionId());
                })
                .toList();
    }
}