package com.prince.notesai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QdrantPoint {

    private Long id;
    private List<Float> vector;
    private QdrantPayload payload;
}