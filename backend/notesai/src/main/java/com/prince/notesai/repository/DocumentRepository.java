package com.prince.notesai.repository;

import com.prince.notesai.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findBySessionId(String sessionId);

}