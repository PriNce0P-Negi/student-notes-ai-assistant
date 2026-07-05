package com.prince.notesai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;

    private String storedFileName;

    private String fileType;

    private Long fileSize;

    private String uploadStatus;

    private LocalDateTime uploadedAt;

    @Column(name = "session_id")
    private String sessionId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(
            mappedBy = "document",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DocumentChunk> chunks;
}