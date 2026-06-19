package com.prince.notesai.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponse {

    private Long documentId;

    private String originalFileName;

    private String message;
}