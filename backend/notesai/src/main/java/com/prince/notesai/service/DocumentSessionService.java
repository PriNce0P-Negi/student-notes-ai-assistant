package com.prince.notesai.service;

import com.prince.notesai.entity.Document;
import com.prince.notesai.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentSessionService {

    private final DocumentRepository documentRepository;

    public String createSession() {
        return UUID.randomUUID().toString();
    }

    public List<Document> getSessionDocuments(String sessionId) {
        return documentRepository.findBySessionId(sessionId);
    }
}