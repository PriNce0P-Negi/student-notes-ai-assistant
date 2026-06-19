package com.prince.notesai.service;

import com.prince.notesai.dto.QdrantPayload;
import com.prince.notesai.dto.QdrantPoint;
import com.prince.notesai.dto.QdrantPointRequest;
import com.prince.notesai.dto.QdrantSearchRequest;
import com.prince.notesai.dto.QdrantSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QdrantService {

    private static final String COLLECTION_NAME = "notes";

    private final WebClient qdrantWebClient;

    public void uploadVector(
            Long pointId,
            Long documentId,
            Integer chunkIndex,
            List<Float> embedding) {

        QdrantPayload payload = new QdrantPayload(
                documentId,
                chunkIndex
        );

        QdrantPoint point = new QdrantPoint(
                pointId,
                embedding,
                payload
        );

        QdrantPointRequest request =
                new QdrantPointRequest(List.of(point));

        qdrantWebClient
                .put()
                .uri("/collections/" + COLLECTION_NAME + "/points")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public QdrantSearchResponse search(
            List<Float> embedding,
            int limit
    ) {

        QdrantSearchRequest request =
                new QdrantSearchRequest(
                        embedding,
                        limit,
                        true
                );

        return qdrantWebClient
                .post()
                .uri("/collections/" + COLLECTION_NAME + "/points/search")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(QdrantSearchResponse.class)
                .block();
    }

    public void checkServer() {

        String response = qdrantWebClient
                .get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println(response);
    }
}