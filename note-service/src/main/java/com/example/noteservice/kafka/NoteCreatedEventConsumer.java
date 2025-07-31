package com.example.noteservice.kafka;

import com.example.noteservice.domain.event.NoteCreatedEvent;
import com.example.noteservice.service.AsyncProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class NoteCreatedEventConsumer {
    
    @Resource
    private AsyncProcessingService asyncProcessingService;
    
    // 添加启动日志
    @PostConstruct
    public void init() {
        log.info("NoteCreatedEventConsumer 已初始化");
    }
    
    @KafkaListener(topics = "note-created-events", containerFactory = "kafkaListenerContainerFactory")
    public void handleNoteCreatedEvent(NoteCreatedEvent event) {
        log.info("接收到笔记创建事件: noteId={}, userId={}, title={}", 
                event.getNoteId(), event.getUserId(), event.getTitle());
        
        // 委托给异步处理服务
        asyncProcessingService.processNoteCreatedEvent(event);
    }
}