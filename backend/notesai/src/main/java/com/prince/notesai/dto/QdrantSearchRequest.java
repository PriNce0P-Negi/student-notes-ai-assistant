package com.prince.notesai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request body sent to Qdrant's /points/search endpoint.
 *
 * Optional {@code filter} field pushes filtering to Qdrant so only
 * matching payload points are ranked, rather than retrieving all and
 * filtering in Java.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QdrantSearchRequest {

    /** The query embedding vector. */
    private List<Float> vector;

    /** Maximum number of results to return. */
    private Integer limit;

    /** Whether to include payload fields in the response. */
    private Boolean with_payload;

    /**
     * Optional payload filter.
     * When set, Qdrant restricts search to points that match the filter
     * before ranking by vector similarity.
     */
    private QdrantFilter filter;
}