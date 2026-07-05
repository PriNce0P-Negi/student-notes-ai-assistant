package com.prince.notesai.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatMemoryService {

    private static final int MAX_TURNS = 10;

    private final Map<String, List<String>> conversations = new LinkedHashMap<>();

    public void addUserMessage(String sessionId, String message) {
        List<String> history = getOrCreate(sessionId);
        history.add("User: " + message);
        trim(history);
    }

    public void addAssistantMessage(String sessionId, String message) {
        List<String> history = getOrCreate(sessionId);
        history.add("Assistant: " + message);
        trim(history);
    }

    public String getConversation(String sessionId) {
        List<String> history = conversations.getOrDefault(sessionId, List.of());
        return String.join("\n\n", history);
    }

    public void clear(String sessionId) {
        conversations.remove(sessionId);
    }

    private List<String> getOrCreate(String sessionId) {
        return conversations.computeIfAbsent(sessionId, k -> new ArrayList<>());
    }

    private void trim(List<String> history) {
        int maxEntries = MAX_TURNS * 2;
        while (history.size() > maxEntries) {
            history.remove(0);
        }
    }
}