package com.prince.notesai.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

            throw new RuntimeException("Embedding generation failed.");
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
                  "text": %s
                }
              ]
            }
          ]
        }
        """.formatted(new com.google.gson.Gson().toJson(prompt));

        String response = geminiWebClient
                .post()
                .uri("/v1beta/models/gemini-2.5-flash:generateContent")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        JsonArray candidates = json.getAsJsonArray("candidates");

        if (candidates == null || candidates.isEmpty()) {
            return "No response generated.";
        }

        JsonObject first = candidates.get(0).getAsJsonObject();

        JsonObject content = first.getAsJsonObject("content");

        JsonArray parts = content.getAsJsonArray("parts");

        return parts.get(0)
                .getAsJsonObject()
                .get("text")
                .getAsString();
    }
}