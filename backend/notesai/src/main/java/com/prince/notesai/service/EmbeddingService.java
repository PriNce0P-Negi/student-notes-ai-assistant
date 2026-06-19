package com.prince.notesai.service;

import com.prince.notesai.dto.EmbeddingRequest;
import com.prince.notesai.dto.EmbeddingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final WebClient geminiWebClient;

    public List<Float> generateEmbedding(String text) {

        EmbeddingRequest.Part part =
                new EmbeddingRequest.Part(text);

        EmbeddingRequest.Content content =
                new EmbeddingRequest.Content(
                        new EmbeddingRequest.Part[]{part}
                );

        EmbeddingRequest request =
                new EmbeddingRequest(
                        "models/gemini-embedding-001",
                        content
                );

        EmbeddingResponse response =
                geminiWebClient
                        .post()
                        .uri("/v1beta/models/gemini-embedding-001:embedContent")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(EmbeddingResponse.class)
                        .block();

        if (response == null
                || response.getEmbedding() == null
                || response.getEmbedding().getValues() == null) {
            throw new RuntimeException("Failed to generate embedding.");
        }

        return response.getEmbedding().getValues();
    }

    public String callGeminiChat(String prompt) {

    String requestBody = """
    {
      "contents": [
        {
          "parts": [
            {
              "text": "%s"
            }
          ]
        }
      ]
    }
    """.formatted(prompt.replace("\"", "\\\""));

    String response = geminiWebClient
            .post()
            .uri("/v1beta/models/gemini-2.5-flash:generateContent")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .block();

    com.google.gson.JsonObject json =
            com.google.gson.JsonParser.parseString(response).getAsJsonObject();

    return json.getAsJsonArray("candidates")
            .get(0).getAsJsonObject()
            .getAsJsonObject("content")
            .getAsJsonArray("parts")
            .get(0).getAsJsonObject()
            .get("text")
            .getAsString();
}
}