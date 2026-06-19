package com.prince.notesai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QdrantSearchResponse {

    private List<SearchResult> result;

    @Getter
    @Setter
    public static class SearchResult {

        private Long id;

        private Float score;

        private QdrantPayload payload;

    }

}