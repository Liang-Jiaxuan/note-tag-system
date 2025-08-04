package com.example.noteservice.kafka;

import com.example.noteservice.domain.event.NoteCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterQueueHandler {

    @KafkaListener(topics = "note-created-events.DLT", groupId = "note-service-dlt-group")
    public void handleDeadLetterMessage(
            @Payload NoteCreatedEvent event,
            @Header(KafkaHeaders.ORIGINAL_TOPIC) String originalTopic,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        
        log.error("处理死信队列消息 - 原始主题: {}, 异常: {}, 事件: {}", 
                 originalTopic, exceptionMessage, event);
        
        // 这里可以添加死信消息的处理逻辑
        // 比如：发送告警、记录到数据库、人工处理等
        handleDeadLetterEvent(event, originalTopic, exceptionMessage);
    }

    private void handleDeadLetterEvent(NoteCreatedEvent event, String originalTopic, String exceptionMessage) {
        // 记录死信消息到数据库或日志
        log.error("死信消息详情 - NoteId: {}, UserId: {}, 原始主题: {}, 异常: {}", 
                 event.getNoteId(), event.getUserId(), originalTopic, exceptionMessage);
        
        // 可以在这里添加告警通知逻辑
        // sendAlertNotification(event, exceptionMessage);
    }
}