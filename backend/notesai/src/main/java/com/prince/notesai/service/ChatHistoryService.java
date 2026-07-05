package com.prince.notesai.service;

import com.prince.notesai.entity.ChatHistory;
import com.prince.notesai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryRepository repository;

    public void save(
            String sessionId,
            String question,
            String answer
    ) {

        repository.save(
                ChatHistory.builder()
                        .sessionId(sessionId)
                        .question(question)
                        .answer(answer)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    public List<ChatHistory> getHistory(
            String sessionId
    ) {

        return repository.findBySessionIdOrderByCreatedAtAsc(
                sessionId
        );
    }

    public void deleteHistory(
            String sessionId
    ) {

        repository.deleteAll(
                repository.findBySessionIdOrderByCreatedAtAsc(
                        sessionId
                )
        );
    }

    public String buildConversation(
            String sessionId
    ) {

        StringBuilder builder = new StringBuilder();

        List<ChatHistory> history =
                repository.findBySessionIdOrderByCreatedAtAsc(
                        sessionId
                );

        for (ChatHistory chat : history) {

            builder.append("User: ")
                    .append(chat.getQuestion())
                    .append("\n");

            builder.append("Assistant: ")
                    .append(chat.getAnswer())
                    .append("\n\n");
        }

        return builder.toString();
    }

}