package com.prince.notesai.service;

import com.prince.notesai.dto.ChatResponse;
import com.prince.notesai.dto.QdrantSearchResponse;
import com.prince.notesai.dto.RelevantChunk;
import com.prince.notesai.dto.SourceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {

    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final RetrievalService retrievalService;
    private final ChatMemoryService chatMemoryService;
    private final ChatHistoryService chatHistoryService;

    public ChatResponse chat(
            String sessionId,
            String question,
            List<Long> documentIds
    ) {
        log.info("[AIChatService] sessionId={} | question={}", sessionId, question);

        List<Float> embedding = embeddingService.generateEmbedding(question);

        QdrantSearchResponse qdrantResponse = qdrantService.search(
                embedding,
                5,
                sessionId,
                documentIds
        );

        List<RelevantChunk> chunks = retrievalService.getRelevantChunks(qdrantResponse);
        log.info("[AIChatService] {} relevant chunks retrieved.", chunks.size());

        String context = chunks.stream()
                .map(RelevantChunk::getContent)
                .reduce("", (a, b) -> a + "\n\n---\n\n" + b);

        String history = chatHistoryService.buildConversation(sessionId);

        String prompt = buildPrompt(history, context, question);

        String answer = embeddingService.callGeminiChat(prompt);

        chatHistoryService.save(sessionId, question, answer);

        chatMemoryService.addUserMessage(sessionId, question);
        chatMemoryService.addAssistantMessage(sessionId, answer);

        List<SourceDto> sources = chunks.stream()
                .map(chunk -> new SourceDto(
                        chunk.getDocumentName(),
                        chunk.getPageNumber(),
                        chunk.getChunkIndex()
                ))
                .distinct()
                .toList();

        return ChatResponse.builder()
                .answer(answer)
                .sources(sources)
                .build();
    }

    public void clearConversation(String sessionId) {
        chatMemoryService.clear(sessionId);
        log.info("[AIChatService] Cleared in-memory cache for session={}", sessionId);
    }

    private String buildPrompt(String history, String context, String question) {
        return """
You are Student Notes AI, an intelligent academic assistant.

=== CONVERSATION HISTORY ===
%s

=== NOTES CONTEXT ===
%s

=== INSTRUCTIONS ===
1. Answer ONLY from the provided Notes Context above.
2. Refer to Conversation History for follow-up context and continuity.
3. If the answer is not found in the notes, say exactly: "I couldn't find that in your uploaded notes."
4. Use clear headings (##), bullet points, and concise explanations.
5. Do NOT fabricate information not present in the notes.
6. Be accurate, structured, and student-friendly.

=== STUDENT QUESTION ===
%s

=== YOUR ANSWER ===
""".formatted(
                history.isBlank() ? "No previous conversation." : history,
                context.isBlank() ? "No relevant notes found." : context,
                question
        );
    }
}