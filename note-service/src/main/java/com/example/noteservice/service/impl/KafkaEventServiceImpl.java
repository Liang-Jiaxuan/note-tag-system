package com.example.noteservice.service.impl;

import com.example.noteservice.domain.event.NoteCreatedEvent;
import com.example.noteservice.service.KafkaEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.Resource;
import java.util.UUID;

@Slf4j
@Service
public class KafkaEventServiceImpl implements KafkaEventService {
    
    private static final String NOTE_CREATED_TOPIC = "note-created-events";
    
    @Resource
    private KafkaTemplate<String, NoteCreatedEvent> kafkaTemplate;
    
    @Override
    public void publishNoteCreatedEvent(NoteCreatedEvent event) {
        try {
            log.info("准备发送笔记创建事件: noteId={}, userId={}, title={}", 
                    event.getNoteId(), event.getUserId(), event.getTitle());
            
            // 发送消息到Kafka，使用note-created类型
            ListenableFuture<SendResult<String, NoteCreatedEvent>> future = 
                    kafkaTemplate.send(NOTE_CREATED_TOPIC, event.getNoteId().toString(), event);
            
            // 添加回调处理
            future.addCallback(
                result -> {
                    log.info("笔记创建事件发送成功: noteId={}, topic={}, partition={}, offset={}", 
                            event.getNoteId(), 
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                },
                ex -> {
                    log.error("笔记创建事件发送失败: noteId={}, error={}", 
                            event.getNoteId(), ex.getMessage(), ex);
                }
            );
        } catch (Exception e) {
            log.error("发送笔记创建事件异常: noteId={}, error={}", 
                    event.getNoteId(), e.getMessage(), e);
        }
    }
}