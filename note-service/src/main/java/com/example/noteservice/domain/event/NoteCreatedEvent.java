package com.example.noteservice.domain.event;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteCreatedEvent {
    private Long noteId;
    private Long userId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String eventType = "NOTE_CREATED";
    private String eventId;
    private Long timestamp;

    // 添加便捷构造函数
    public NoteCreatedEvent(Long noteId, Long userId, String title, String content, LocalDateTime createdAt) {
        this.noteId = noteId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.eventType = "NOTE_CREATED";
        this.eventId = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }
}