package com.prince.notesai.memory;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConversationMemory {

    private final Map<String, List<String>> conversations =
            new HashMap<>();

    public void addUserMessage(
            String sessionId,
            String message
    ) {

        conversations
                .computeIfAbsent(
                        sessionId,
                        k -> new ArrayList<>()
                )
                .add("User: " + message);
    }

    public void addAIMessage(
            String sessionId,
            String message
    ) {

        conversations
                .computeIfAbsent(
                        sessionId,
                        k -> new ArrayList<>()
                )
                .add("Assistant: " + message);
    }

    public String getConversation(
            String sessionId
    ) {

        List<String> history =
                conversations.getOrDefault(
                        sessionId,
                        Collections.emptyList()
                );

        return String.join("\n\n", history);
    }

    public void clear(
            String sessionId
    ) {

        conversations.remove(sessionId);
    }
}