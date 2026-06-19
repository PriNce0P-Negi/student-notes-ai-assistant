package com.prince.notesai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class QdrantConfig {

    @Bean
    public WebClient qdrantWebClient() {

        return WebClient.builder()
                .baseUrl("http://localhost:6333")
                .build();

    }

}