package com.prince.notesai.service;

import com.prince.notesai.dto.QdrantSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final RetrievalService retrievalService;

    public String chat(String question) {

        List<Float> embedding =
                embeddingService.generateEmbedding(question);

        QdrantSearchResponse response =
                qdrantService.search(embedding, 5);

        List<String> chunks =
                retrievalService.getRelevantChunks(response);

        String context = String.join("\n\n", chunks);

        String prompt =
                """
                You are a helpful AI assistant.

                Answer ONLY from the context below.

                If the answer is not present, reply:
                "I could not find that information in the uploaded notes."

                Context:
                %s

                Question:
                %s
                """.formatted(context, question);

        return embeddingService.callGeminiChat(prompt);
    }
}