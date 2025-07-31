package com.example.noteservice.service.impl;

import com.example.noteservice.domain.event.NoteCreatedEvent;
import com.example.noteservice.service.AsyncProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class AsyncProcessingServiceImpl implements AsyncProcessingService {
    
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final ConcurrentHashMap<String, Long> processedNotes = new ConcurrentHashMap<>();
    
    @Override
    public void processNoteCreatedEvent(NoteCreatedEvent event) {
        try {
            log.info("开始处理笔记创建事件: noteId={}, userId={}, title={}", 
                    event.getNoteId(), event.getUserId(), event.getTitle());
            
            // 模拟异步处理任务
            Thread.sleep(1000); // 模拟处理时间
            
            // 更新统计信息
            processedCount.incrementAndGet();
            processedNotes.put(event.getNoteId().toString(), System.currentTimeMillis());
            
            log.info("笔记创建事件处理完成: noteId={}", event.getNoteId());
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            log.error("处理笔记创建事件失败: noteId={}, error={}", 
                    event.getNoteId(), e.getMessage(), e);
        }
    }
    
    @Override
    public long getProcessedCount() {
        return processedCount.get();
    }
    
    @Override
    public long getErrorCount() {
        return errorCount.get();
    }
    
    @Override
    public int getProcessedNotesCount() {
        return processedNotes.size();
    }
    
    @Override
    public void resetStatistics() {
        processedCount.set(0);
        errorCount.set(0);
        processedNotes.clear();
    }
}