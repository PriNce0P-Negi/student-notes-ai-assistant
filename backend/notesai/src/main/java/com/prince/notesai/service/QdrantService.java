package com.prince.notesai.service;

import com.prince.notesai.dto.QdrantFilter;
import com.prince.notesai.dto.QdrantPayload;
import com.prince.notesai.dto.QdrantPoint;
import com.prince.notesai.dto.QdrantPointRequest;
import com.prince.notesai.dto.QdrantSearchRequest;
import com.prince.notesai.dto.QdrantSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QdrantService {

    private static final String COLLECTION_NAME = "notes";

    private final WebClient qdrantWebClient;

    @PostConstruct
    public void initCollection() {
        try {
            qdrantWebClient.get()
                    .uri("/collections/" + COLLECTION_NAME)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("[Qdrant] Collection '{}' exists.", COLLECTION_NAME);
        } catch (WebClientResponseException.NotFound e) {
            log.info("[Qdrant] Collection '{}' not found. Creating...", COLLECTION_NAME);

            String requestBody = """
                {
                  "vectors": {
                    "size": 3072,
                    "distance": "Cosine"
                  }
                }
                """;

            qdrantWebClient.put()
                    .uri("/collections/" + COLLECTION_NAME)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("[Qdrant] Collection '{}' created.", COLLECTION_NAME);
        } catch (Exception e) {
            log.warn("[Qdrant] Failed to check/create collection. Is Qdrant running? ({})", e.getMessage());
        }
    }

    public void uploadVector(
            Long pointId,
            String sessionId,
            Long documentId,
            Integer chunkIndex,
            Integer pageNumber,
            List<Float> embedding
    ) {
        QdrantPayload payload = new QdrantPayload(sessionId, documentId, chunkIndex, pageNumber);
        QdrantPoint point = new QdrantPoint(pointId, embedding, payload);
        QdrantPointRequest request = new QdrantPointRequest(List.of(point));

        String result = qdrantWebClient
                .put()
                .uri("/collections/" + COLLECTION_NAME + "/points")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.debug("[Qdrant] Upserted point {} → {}", pointId, result);
    }

    public QdrantSearchResponse search(
            List<Float> embedding,
            int limit,
            String sessionId,
            List<Long> documentIds
    ) {
        QdrantFilter filter = buildFilter(sessionId, documentIds);

        QdrantSearchRequest request = new QdrantSearchRequest(
                embedding,
                limit,
                true,
                filter
        );

        log.debug("[Qdrant] Searching — limit={}, sessionId={}, docIds={}",
                limit, sessionId, documentIds);

        return qdrantWebClient
                .post()
                .uri("/collections/" + COLLECTION_NAME + "/points/search")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(QdrantSearchResponse.class)
                .block();
    }

    public QdrantSearchResponse search(List<Float> embedding, int limit) {
        return search(embedding, limit, null, null);
    }

    public void checkServer() {
        String response = qdrantWebClient
                .get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("[Qdrant] Server response: {}", response);
    }

    private QdrantFilter buildFilter(String sessionId, List<Long> documentIds) {
        List<QdrantFilter.Condition> conditions = new ArrayList<>();

        if (sessionId != null && !sessionId.isBlank()) {
            conditions.add(new QdrantFilter.Condition(
                    "sessionId",
                    QdrantFilter.Match.exact(sessionId)
            ));
        }

        if (documentIds != null && !documentIds.isEmpty()) {
            conditions.add(new QdrantFilter.Condition(
                    "documentId",
                    QdrantFilter.Match.anyOf(documentIds)
            ));
        }

        return conditions.isEmpty() ? null : new QdrantFilter(conditions);
    }
}