package com.prince.notesai.service;

import com.prince.notesai.dto.QdrantSearchResponse;
import com.prince.notesai.entity.DocumentChunk;
import com.prince.notesai.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrievalService {

    private final DocumentChunkRepository chunkRepository;

    public List<String> getRelevantChunks(QdrantSearchResponse response) {

        List<String> chunks = new ArrayList<>();

        if (response == null || response.getResult() == null) {
            return chunks;
        }

        for (QdrantSearchResponse.SearchResult result : response.getResult()) {

            Long documentId = result.getPayload().getDocumentId();
            Integer chunkIndex = result.getPayload().getChunkIndex();

            chunkRepository
                    .findByDocumentIdAndChunkIndex(documentId, chunkIndex)
                    .ifPresent(chunk -> chunks.add(chunk.getContent()));
        }

        return chunks;
    }
}