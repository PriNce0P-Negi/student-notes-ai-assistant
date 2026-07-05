package com.prince.notesai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SourceDto {

    private String documentName;

    private Integer pageNumber;

    private Integer chunkIndex;
}