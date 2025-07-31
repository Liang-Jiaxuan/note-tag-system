package com.example.noteservice.service;

import com.example.noteservice.domain.event.NoteCreatedEvent;

public interface AsyncProcessingService {
    /**
     * 处理笔记创建事件
     * @param event 笔记创建事件
     */
    void processNoteCreatedEvent(NoteCreatedEvent event);
    
    /**
     * 获取已处理事件数量
     * @return 处理数量
     */
    long getProcessedCount();
    
    /**
     * 获取错误事件数量
     * @return 错误数量
     */
    long getErrorCount();
    
    /**
     * 获取已处理笔记数量
     * @return 笔记数量
     */
    int getProcessedNotesCount();
    
    /**
     * 重置统计信息
     */
    void resetStatistics();
}