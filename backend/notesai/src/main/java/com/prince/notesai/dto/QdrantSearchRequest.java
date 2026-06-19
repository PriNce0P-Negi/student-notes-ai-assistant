package com.prince.notesai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QdrantSearchRequest {

    private List<Float> vector;

    private Integer limit;

    private Boolean with_payload;

}