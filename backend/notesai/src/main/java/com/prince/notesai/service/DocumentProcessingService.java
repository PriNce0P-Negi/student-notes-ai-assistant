package com.prince.notesai.service;

import com.prince.notesai.entity.Document;
import com.prince.notesai.entity.DocumentChunk;
import com.prince.notesai.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private final TextChunkingService textChunkingService;
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final DocumentChunkRepository chunkRepository;

    public void process(Document document, String pdfText) {

        List<String> chunks = textChunkingService.splitIntoChunks(pdfText);

        System.out.println("=================================");
        System.out.println("Generating Embeddings...");
        System.out.println("=================================");

        for (int i = 0; i < chunks.size(); i++) {

            String chunkText = chunks.get(i);

            DocumentChunk chunk = DocumentChunk.builder()
                    .document(document)
                    .chunkIndex(i)
                    .content(chunkText)
                    .build();

            chunk = chunkRepository.save(chunk);

            List<Float> embedding =
                    embeddingService.generateEmbedding(chunkText);

            qdrantService.uploadVector(
                    chunk.getId(),
                    document.getId(),
                    i,
                    embedding
            );

            chunk.setVectorId(String.valueOf(chunk.getId()));
            chunkRepository.save(chunk);

            System.out.println("Chunk : " + (i + 1));
            System.out.println("Chunk ID : " + chunk.getId());
            System.out.println("Embedding Dimension : " + embedding.size());
        }

        System.out.println("=================================");
        System.out.println("All Chunks Processed Successfully");
        System.out.println("=================================");
    }
}