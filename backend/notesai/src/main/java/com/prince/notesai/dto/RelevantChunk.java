package com.prince.notesai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelevantChunk {

    private Long documentId;

    private String documentName;

    private Long chunkId;

    private Integer chunkIndex;

    private Integer pageNumber;

    private Double score;

    private String content;
}