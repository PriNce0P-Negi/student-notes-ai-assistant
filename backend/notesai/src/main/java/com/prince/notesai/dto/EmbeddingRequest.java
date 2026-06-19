package com.prince.notesai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddingRequest {

    private String model;
    private Content content;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Content {

        private Part[] parts;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Part {

        private String text;

    }

}