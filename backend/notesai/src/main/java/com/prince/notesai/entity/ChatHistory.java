package com.prince.notesai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;

    @Column(columnDefinition = "LONGTEXT")
    private String question;

    @Column(columnDefinition = "LONGTEXT")
    private String answer;

    private LocalDateTime createdAt;
}