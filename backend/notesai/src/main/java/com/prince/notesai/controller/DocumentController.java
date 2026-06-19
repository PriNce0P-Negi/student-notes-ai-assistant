package com.prince.notesai.controller;

import com.prince.notesai.dto.ChatRequest;
import com.prince.notesai.dto.ChatResponse;
import com.prince.notesai.dto.QdrantSearchResponse;
import com.prince.notesai.dto.UploadResponse;
import com.prince.notesai.entity.Document;
import com.prince.notesai.service.EmbeddingService;
import com.prince.notesai.service.FileStorageService;
import com.prince.notesai.service.QdrantService;
import com.prince.notesai.service.RetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final FileStorageService fileStorageService;
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final RetrievalService retrievalService;

    @PostMapping("/upload")
    public UploadResponse upload(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        Document document = fileStorageService.saveFile(file);

        return UploadResponse.builder()
                .documentId(document.getId())
                .originalFileName(document.getOriginalFileName())
                .message("File uploaded successfully")
                .build();
    }

    @GetMapping("/search")
    public List<String> search(@RequestParam String query) {

        List<Float> embedding =
                embeddingService.generateEmbedding(query);

        QdrantSearchResponse response =
                qdrantService.search(embedding, 5);

        return retrievalService.getRelevantChunks(response);
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        List<Float> embedding =
                embeddingService.generateEmbedding(request.getQuestion());

        QdrantSearchResponse response =
                qdrantService.search(embedding, 5);

        List<String> chunks =
                retrievalService.getRelevantChunks(response);

        String context = String.join("\n\n", chunks);

        String prompt =
                """
                Answer ONLY from the context below.

                Context:
                %s

                Question:
                %s
                """.formatted(context, request.getQuestion());

        String answer =
                embeddingService.callGeminiChat(prompt);

        return new ChatResponse(answer);
    }
}