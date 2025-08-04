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
    private final ConcurrentHashMap<Long, String> processedNotes = new ConcurrentHashMap<>();

    @Override
    public void processNoteCreatedEvent(NoteCreatedEvent event) {
        try {
            log.info("开始处理笔记创建事件: noteId={}, userId={}, title={}",
                    event.getNoteId(), event.getUserId(), event.getTitle());

            // 模拟调用外部服务（可能失败的操作）
            boolean externalServiceSuccess = callExternalService(event);

            if (!externalServiceSuccess) {
                // 如果外部服务调用失败，抛出异常触发重试机制
                throw new RuntimeException("外部服务调用失败: " + event.getNoteId());
            }

            // 模拟后台处理任务
            Thread.sleep(1000);

            // 记录处理成功的笔记
            processedNotes.put(event.getNoteId(), event.getTitle());
            processedCount.incrementAndGet();

            log.info("笔记创建事件处理完成: noteId={}", event.getNoteId());

        } catch (Exception e) {
            errorCount.incrementAndGet();
            log.error("处理笔记创建事件失败: noteId={}, 错误: {}", event.getNoteId(), e.getMessage(), e);

            // 重新抛出异常，让Kafka重试机制处理
            throw new RuntimeException("异步处理失败: " + e.getMessage(), e);
        }
    }

    private boolean callExternalService(NoteCreatedEvent event) {
        // 模拟外部服务调用，随机失败
        double random = Math.random();
        if (random < 0.3) { // 30%概率失败
            log.warn("模拟外部服务调用失败: noteId={}", event.getNoteId());
            return false;
        }

        log.info("模拟外部服务调用成功: noteId={}", event.getNoteId());
        return true;
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
    public long getProcessedNotesCount() {
        return processedNotes.size();
    }

    @Override
    public void resetStatistics() {
        processedCount.set(0);
        errorCount.set(0);
        processedNotes.clear();
    }
}