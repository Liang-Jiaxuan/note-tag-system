package com.example.noteservice.service;

import com.example.noteservice.domain.event.NoteCreatedEvent;

public interface KafkaEventService {
    /**
     * 发布笔记创建事件
     * @param event 笔记创建事件
     */
    void publishNoteCreatedEvent(NoteCreatedEvent event);
}