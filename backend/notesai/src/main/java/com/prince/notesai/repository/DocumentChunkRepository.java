package com.prince.notesai.repository;

import com.prince.notesai.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    Optional<DocumentChunk> findByVectorId(String vectorId);

    Optional<DocumentChunk> findByDocumentIdAndChunkIndex(
            Long documentId,
            Integer chunkIndex
    );

}