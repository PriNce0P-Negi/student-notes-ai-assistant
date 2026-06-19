package com.prince.notesai.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer chunkIndex;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "vector_id")
    private String vectorId;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;
}