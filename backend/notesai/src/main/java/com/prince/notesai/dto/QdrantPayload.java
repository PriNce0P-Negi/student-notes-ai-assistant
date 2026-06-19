package com.prince.notesai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QdrantPayload {

    private Long documentId;
    private Integer chunkIndex;
}