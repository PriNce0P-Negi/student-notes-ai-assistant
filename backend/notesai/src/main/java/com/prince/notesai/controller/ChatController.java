package com.prince.notesai.controller;

import com.prince.notesai.dto.ChatRequest;
import com.prince.notesai.dto.ChatResponse;
import com.prince.notesai.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AIChatService aiChatService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String answer = aiChatService.chat(request.getQuestion());

        return new ChatResponse(answer);
    }
}