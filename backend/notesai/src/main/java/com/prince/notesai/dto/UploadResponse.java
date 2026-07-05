package com.prince.notesai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponse {

    private Long documentId;

    private String sessionId;

    private String originalFileName;

    private String message;

}