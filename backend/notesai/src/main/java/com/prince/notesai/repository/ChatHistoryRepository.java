package com.prince.notesai.repository;

import com.prince.notesai.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatHistoryRepository
        extends JpaRepository<ChatHistory, Long> {

}