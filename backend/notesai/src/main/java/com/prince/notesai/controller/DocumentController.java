package com.prince.notesai.controller;

import com.prince.notesai.dto.ChatRequest;
import com.prince.notesai.dto.ChatResponse;
import com.prince.notesai.dto.QdrantSearchResponse;
import com.prince.notesai.dto.RelevantChunk;
import com.prince.notesai.dto.UploadResponse;
import com.prince.notesai.entity.ChatHistory;
import com.prince.notesai.entity.Document;
import com.prince.notesai.service.AIChatService;
import com.prince.notesai.service.ChatHistoryService;
import com.prince.notesai.service.DocumentSessionService;
import com.prince.notesai.service.EmbeddingService;
import com.prince.notesai.service.FileStorageService;
import com.prince.notesai.service.QdrantService;
import com.prince.notesai.service.RetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentController {

    private final FileStorageService fileStorageService;
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final RetrievalService retrievalService;
    private final ChatHistoryService chatHistoryService;
    private final DocumentSessionService documentSessionService;
    private final AIChatService aiChatService;

    @PostMapping("/sessions")
    public ResponseEntity<String> createSession() {
        String sessionId = documentSessionService.createSession();
        log.info("[Session] Created new session: {}", sessionId);
        return ResponseEntity.ok(sessionId);
    }

    @GetMapping("/sessions/{sessionId}/documents")
    public ResponseEntity<List<Document>> getSessionDocuments(
            @PathVariable String sessionId
    ) {
        List<Document> docs = documentSessionService.getSessionDocuments(sessionId);
        return ResponseEntity.ok(docs);
    }

    @GetMapping("/sessions/{sessionId}/history")
    public ResponseEntity<List<ChatHistory>> getChatHistory(
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(chatHistoryService.getHistory(sessionId));
    }

    @DeleteMapping("/sessions/{sessionId}/history")
    public ResponseEntity<String> clearHistory(
            @PathVariable String sessionId
    ) {
        aiChatService.clearConversation(sessionId);
        chatHistoryService.deleteHistory(sessionId);
        log.info("[Session] Cleared history for session: {}", sessionId);
        return ResponseEntity.ok("Chat history cleared.");
    }

    @PostMapping("/documents/upload")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sessionId", required = false) String sessionId
    ) throws Exception {

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = documentSessionService.createSession();
            log.info("[Upload] No sessionId provided — created new session: {}", sessionId);
        }

        log.info("[Upload] Uploading file: {} for session: {}",
                file.getOriginalFilename(), sessionId);

        Document document = fileStorageService.saveFile(file, sessionId);

        return ResponseEntity.ok(
                UploadResponse.builder()
                        .documentId(document.getId())
                        .sessionId(document.getSessionId())
                        .originalFileName(document.getOriginalFileName())
                        .message("File uploaded and processed successfully.")
                        .build()
        );
    }

    @GetMapping("/documents/search")
    public ResponseEntity<List<RelevantChunk>> search(
            @RequestParam String query,
            @RequestParam String sessionId
    ) {
        List<Float> embedding = embeddingService.generateEmbedding(query);
        QdrantSearchResponse qdrantResponse = qdrantService.search(embedding, 5);
        List<RelevantChunk> results = retrievalService.getRelevantChunks(
                qdrantResponse,
                sessionId
        );
        return ResponseEntity.ok(results);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request
    ) {
        log.info("[Chat] sessionId={}, question={}",
                request.getSessionId(), request.getQuestion());

        ChatResponse response = aiChatService.chat(
                request.getSessionId(),
                request.getQuestion(),
                request.getDocumentIds()
        );

        return ResponseEntity.ok(response);
    }
}